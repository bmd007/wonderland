# Revolut: Event Sourcing with PostgreSQL at Scale

## The 30-Second Version

Revolut built a custom event-streaming platform on PostgreSQL instead of Kafka. They store events in an append-only Postgres table, use LISTEN/NOTIFY for real-time delivery, and read replicas for historical replay. Redis tracks consumer offsets. The system processes 37+ billion events per month across 12+ billion stored records.

---

## Overview

Revolut is a neobank with millions of users. Their internal system, called EventStore, is one of the architectural backbones of the company. They chose PostgreSQL over Kafka for one critical reason: **ad-hoc SQL queries on events**. In a financial system, being able to run `SELECT * FROM events WHERE payload->>'customer_id' = '12345'` is invaluable for debugging, fraud detection, and compliance.

### Why Not Kafka?

- Steep operational learning curve
- Less suitable for arbitrary historical queries
- Configuration complexity for their financial use case
- Postgres gives them ACID transactions, SQL querying, and native replication — all things they'd need to build on top of Kafka anyway

---

## Database Topology

### Two-Cluster Architecture

```
Main Event Store Cluster
  Master (writes only)
  Read Replicas (streaming + queries)
  12+ billion records
  +1 billion records/month
  12 months of heavily indexed data
        |
        v  (cold archival)
Archive Cluster
  Master + Read Replicas
  4+ years of event history
  Rarely accessed, lower-resource hardware
```

Why two clusters: performance isolation between hot and cold data, separate replication topologies, cost optimization.

### Partitioning Strategy

- **Method**: Native PostgreSQL partitioning via `pg_partman`
- **Partition key**: Timestamp (monthly partitions)
- **Philosophy**: Treat partitions as immutable blocks (similar to Cassandra SSTables)
- Old partitions are moved to the archive cluster
- Indexing and vacuuming run in parallel on smaller partition tables

A key insight: **partitioning is applied dynamically per subscription at runtime**, not fixed at the table level. This lets them repartition without downtime and apply different partitioning strategies per consumer.

---

## Event Persistence: The Dual-Write Pattern

This is where Revolut diverges from classical event sourcing. Instead of "generate event, then derive state," they do:

```
BEGIN TRANSACTION;
  UPDATE account_state SET blocked = true WHERE account_id = X;
  INSERT INTO events (type, payload, account_id) VALUES ('CardBlocked', {...}, X);
COMMIT;
```

**Model change and event are saved in the same transaction.** If the transaction rolls back, both are discarded. No orphaned events, no lost state changes.

### Why This Matters

In classical event sourcing (what hesab-ketab does), the event IS the state change — you derive state by replaying events. In Revolut's model, both the state AND the event are first-class. The event serves as an audit log and a change notification, but the state table is also directly queryable.

**Trade-off**: You lose the purity of "events are the only source of truth." You gain simpler reads (no replay needed) and guaranteed consistency between state and events. For a neobank doing millions of real-time transactions, this pragmatic choice makes sense.

### EventLog Table

Events pending publication are stored in a temporary EventLog table with a 24-hour TTL. A background reconciliation process retries any event older than 30 seconds that hasn't been marked as "published." This ensures at-least-once delivery even if the real-time NOTIFY path fails.

---

## Streaming: LISTEN/NOTIFY + Offline Replay

### Dual-Phase Consumption

Consumers receive events in two phases:

**Phase 1: Offline Stream (Historical Catch-Up)**

```
Consumer: "Give me events since timestamp T"
  -> EventStore queries READ REPLICA for events before "now"
  -> Before executing: polls replica status to verify data is present
  -> Only falls back to master as last resort
  -> Streams results via Scrollable JDBC ResultSet (cursor-based)
  -> Delivered as Project Reactor Flux (reactive backpressure)
```

The replica check is critical: it prevents querying a lagging replica that would return incomplete results.

**Phase 2: Live Stream (Real-Time)**

```
Once offline stream is exhausted:
  -> Switch to LISTEN/NOTIFY on master
  -> Receive events as they're inserted (via AFTER INSERT trigger)
  -> Concatenate with the historical stream
  -> Single unified event stream to consumer
```

**The seam between offline and live is handled by overlapping**: the offline query includes events up to "now," and the live stream starts from slightly before "now." Duplicates are filtered by sequence number at the consumer.

### How LISTEN/NOTIFY Is Used

```sql
-- Publisher side (trigger on event insert)
NOTIFY event_channel, json_payload;

-- Consumer side (Raw-Live-Stream-Publisher)
LISTEN event_channel;
```

Characteristics:
- FIFO ordering maintained per channel
- All events published (no filtering at channel level — filtering is per-subscription)
- Notifications are NOT durable (live stream only — that's what the offline replay is for)

### Hesab-Ketab Comparison

Your implementation uses the same LISTEN/NOTIFY pattern as Revolut for the wake-up signal. The key difference: Revolut has a sophisticated two-phase approach (replay from replica, then switch to live). Your `catchUp()` on startup achieves the same effect more simply — fetch all unprocessed events from the offset, then wait for notifications.

---

## Consumer Architecture

### Offset Tracking: Redis

Unlike hesab-ketab (which stores offsets in PostgreSQL), Revolut uses Redis:

```
Redis stores:
  - Consumer group offset (monotonically increasing sequence number)
  - Per-partition lease/lock (mutual exclusion)
  - Subscription state
```

Offsets can be specified as either a numeric sequence or a timestamp.

### Why Redis Instead of Postgres?

Consumer offsets are updated on every event — high write frequency, low durability requirement (you can always replay from the event store). Redis handles this write pattern more efficiently than Postgres. The event store itself provides durability; the offset is just a cursor.

### Consumer Types

- **SingleEventConsumer**: Filters by model type + event type + optional payload attributes
- **MultiEventConsumer**: Subscribes to multiple event types from same model
- **Event-Processor**: Orchestration layer that merges subscriptions, dispatches events, manages partition execution

### Partition Execution Model

```
Sequential within partition  (ordering preserved)
Parallel across partitions   (throughput scaled)
```

Multiple event-processor instances compete for partition assignment via Redis leases. When a lease expires, the partition is reassigned to another processor — enabling failover without explicit coordination.

**Hesab-Ketab Comparison**: This is exactly the Kafka-style partitioning described in your `kafka-style-partitioning.md`. Revolut uses Redis leases for partition assignment instead of static hash-based assignment. Their approach handles dynamic scaling better; yours is simpler.

---

## Streaming Protocol: RSocket

Revolut uses RSocket over TCP for event delivery, which extends Reactive Streams semantics to the network.

### Backpressure

```
Client requests: 4,000 events
EventStore delivers: 4,000 events
At 75% consumption (3,000 processed):
  Client asynchronously requests 3,000 more
  Server-side work gated by client demand
```

The server never overwhelms slow clients. No buffering explosions, no OOM kills. The client controls the pace.

**Hesab-Ketab Comparison**: Your one-event-at-a-time model is the simplest form of backpressure — you never request more than you can handle. Revolut's approach is more efficient (batched delivery) but requires the reactive infrastructure.

---

## Consistency Guarantees

### Financial Integrity

Revolut's core principle: **money is sensitive — data integrity > availability.** Even if responsiveness suffers, balances must be correct.

### Idempotency Boundaries (Layered)

1. **API layer**: Request deduplication
2. **Intent layer**: Unique intent ID per operation
3. **Ledger layer**: Double-entry bookkeeping validation
4. **Partner layer**: External system coordination

### Guarantees Provided

- **Strong consistency**: Between model and events (atomic transactions)
- **At-least-once delivery**: Via EventLog reconciliation (24h retry window)
- **FIFO ordering**: Per partition, per subscription
- **Idempotent processing**: Enforced at multiple boundaries

**Hesab-Ketab Comparison**: Your `AccountRepository.save()` uses `WHERE accounts.version < :version` for idempotent projection updates. Revolut's multi-layer approach is more comprehensive but follows the same principle: make it safe to process the same event twice.

---

## Performance

| Metric | Value |
|--------|-------|
| Events processed | 37+ billion/month |
| Events ingested | ~1 billion/month |
| Stored records | 12+ billion |
| Event creation to delivery | ~20ms |
| EventStore to consumer | <10ms |
| End-to-end | ~30ms |
| Growth | 10x year-over-year |

---

## Known Limitations and Future Plans

### Current Limitation

High dependency on single master for LISTEN/NOTIFY. All notifications flow through one node.

### Planned Enhancement

Logical replication topology with multiple master nodes. Triggers on logical replicas would distribute LISTEN connections across the cluster, reducing pressure on the primary.

---

## Architecture Summary

```
Producers (microservices)
  |
  | BEGIN; UPDATE state; INSERT event; COMMIT;
  v
PostgreSQL Master (writes)
  |
  |-- AFTER INSERT trigger --> NOTIFY
  |-- Replication --> Read Replicas (offline queries)
  v
Raw-Live-Stream-Publishers
  |
  |-- RSocket --> Event-Processors
  |                 |
  |                 |-- Redis (offset tracking, partition leases)
  |                 |-- Sequential per partition, parallel across
  |                 v
  |              Consumers (SingleEvent / MultiEvent)
  v
Archive Cluster (4+ years, cold storage)
```

## Sources

- [Recording more events… But where will we store them? — Revolut Tech / Medium](https://medium.com/revolut/recording-more-events-but-where-will-we-store-them-4b1dad457cf5)
- [Event Streaming: The Revolut way — Revolut Tech / Medium](https://medium.com/revolut/event-streaming-the-revolut-way-9d928005ddf7)
