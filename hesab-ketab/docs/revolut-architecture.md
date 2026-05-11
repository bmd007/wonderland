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

#### What "Dynamically Per Subscription" Means

The physical table partitioning (monthly by timestamp via `pg_partman`) is separate from **consumer partitioning** — how events are distributed across consumer instances for parallel processing.

In Kafka, partitions are baked into the topic definition. If you create a topic with 12 partitions keyed by `account_id`, every consumer sees those same 12 partitions. Changing the partition count or key requires creating a new topic and migrating data.

In Revolut's system, all events live in a single queryable Postgres table. The "partitioning" a consumer sees is a **logical construct applied at query time**:

- Consumer A can partition the same events by `account_id % 8` (8 logical partitions)
- Consumer B can partition them by `currency` (a handful of partitions)
- Consumer C can use a completely different key or partition count

This works because events are in SQL — any `WHERE` clause or hash function can be applied at read time. Partition assignment is then tracked via Redis leases.

| Concern | Kafka | Revolut's EventStore |
|---|---|---|
| Partition key | Fixed per topic | Chosen per subscription |
| Partition count | Fixed at creation | Dynamic, changeable at runtime |
| Repartitioning | New topic + data migration | Just change the query/config |
| Downtime to repartition | Yes (or complex migration) | No |

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

#### NOTIFY Is a Broadcast Wake-Up Signal, Not an Event Delivery Mechanism

PostgreSQL LISTEN/NOTIFY is a **pure broadcast**: every session that has executed `LISTEN` on a channel receives every `NOTIFY` on that channel. There are no consumer groups, no selective delivery, no built-in offsets, and no acknowledgments. From the [PostgreSQL NOTIFY docs](https://www.postgresql.org/docs/current/sql-notify.html):

> *"The NOTIFY command sends a notification event together with an optional 'payload' string to each client application that has previously executed LISTEN for the specified channel name in the current database. Notifications are visible to all users."*

Notifications are also **non-durable** — if a client is disconnected when a NOTIFY fires, that notification is lost for that client. PostgreSQL does maintain an internal notification queue (default 8GB), but only for currently-connected listeners that haven't consumed yet. A slow listener can block the queue for everyone.

In Revolut's architecture, NOTIFY acts strictly as a **wake-up signal** — "there are new events." The AFTER INSERT trigger publishes to a Postgres channel that "acts as a queue (fifo), maintaining the insertion order." The Raw-Live-Stream-Publisher listens on this channel, but the actual event data is then **fetched from the database**, not read from the notification payload.

#### So Does Every Consumer Get Every Event?

No — filtering happens at two layers:

1. **Raw-Live-Stream-Publisher layer**: The Raw-Live-Stream-Publisher receives all broadcasts (because LISTEN/NOTIFY is broadcast). It retrieves the actual event data from the database.

2. **Subscription layer**: Different subscriptions' Live-Streams subscribe to the same Raw-Live-Stream-Publisher and **apply a set of filters** that verify "only the events from certain partitions, model-types, event-types and even specific properties in the payload are valid, before sending the event to the client." Each `SingleEventConsumer` is configured with a model-type, event-type, and optionally a payload-filter.

So while PostgreSQL broadcasts the NOTIFY to all listeners, the application layer ensures each consumer only processes events matching its subscription criteria.

#### Offset-Based Deduplication Is Primary, Idempotency Is the Safety Net

Consumer offsets in Redis serve as a **unified cursor across both phases**. When a NOTIFY wake-up arrives during the live phase, the consumer doesn't blindly process whatever it receives — it queries the events table starting from its last known offset (`WHERE event_id > last_offset`). This means:

- Events already processed during the offline phase are naturally skipped
- The overlapping window between offline and live is handled by offset comparison, not by receiving "only unprocessed events"
- Consumers can resume from any point based on their stored timestamp/sequence

Idempotent processing is the **correctness guarantee** for edge cases: crashes between processing and offset commit, multiple instances during deployment, or reconnection after LISTEN/NOTIFY disconnection. But the offset is what prevents routine reprocessing.

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

#### What If Redis Loses All Offset Data?

The answer is surprisingly benign: **no event data is lost**. Events remain intact in PostgreSQL's durable, append-only event store. The consumer simply doesn't know where it left off — it must re-process events from the beginning or from a known timestamp.

This is a **liveness/efficiency** problem, not a **correctness/durability** problem, because:

1. Events are the source of truth (in durable Postgres, replicated, backed up).
2. Consumers are idempotent — reprocessing the same event produces the same result.
3. Consumers can resume from a timestamp rather than replaying everything (e.g., `WHERE created_at >= :last_known_healthy_time - :safety_margin`).

This is why Revolut explicitly treats offsets as low-durability data. The offset is a **bookmark** — losing a bookmark in a book you still own is an inconvenience, not a catastrophe.

Redis persistence (RDB snapshots or AOF) can mitigate this further — after a crash, you'd replay at most a few minutes of events rather than the entire history. But even without persistence, the system recovers by replaying.

**Kafka comparison** *(not Revolut-specific)*: Kafka stores consumer offsets in the `__consumer_offsets` internal topic, replicated across brokers (default replication factor 3). If that topic is lost (catastrophic multi-broker failure), recovery depends on `auto.offset.reset`: set to `earliest`, consumers replay from the beginning (same as Revolut); set to `latest`, consumers skip to the current head, **losing all unprocessed events** — the dangerous option. The recovery model is fundamentally the same: replay from a known position. Revolut trades replication-based durability for replay-based durability, which is a valid trade-off when events are already in a durable store.

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

#### How Kafka Guarantees Ordering Within a Partition (For Comparison)

A Kafka partition is an **append-only, immutable, ordered sequence of records**. Ordering comes from three reinforcing mechanisms:

1. **Single leader per partition**: Only one broker (the partition leader) accepts writes for a given partition. All writes are serialized through a single node — no concurrent appends producing ambiguous order.
2. **Monotonically increasing, gapless offsets**: Each record is assigned an offset (0, 1, 2, 3, ...) by the broker at append time. The offset is the physical position in the log. As [Jay Kreps](https://engineering.linkedin.com/distributed-systems/log-what-every-software-engineer-should-know-about-real-time-datas-unifying) puts it: "The ordering of records defines a notion of 'time' since entries to the left are defined to be older than entries to the right."
3. **Idempotent producer protocol** (since Kafka 0.11): The broker tracks a sequence number per producer per partition. If retries cause records to arrive out of order, the broker rejects the out-of-order batch, forcing the producer to resend in order. This means `max.in.flight.requests.per.connection` up to 5 is safe with `enable.idempotence=true`.

The consumer group protocol ensures **exactly one consumer per partition per group** via a coordinator broker that runs partition assignment (Range/Sticky/CooperativeSticky). If a consumer fails to heartbeat within `session.timeout.ms`, a rebalance reassigns all its partitions.

From the [Kafka documentation](https://kafka.apache.org/intro): "Kafka guarantees that any consumer of a given topic-partition will always read that partition's events in exactly the same order as they were written."

#### How Revolut Achieves Ordering With PostgreSQL

Revolut stores events in a PostgreSQL table with a **monotonically increasing sequence ID** (`BIGSERIAL` or a `SEQUENCE`-backed column). A consumer querying its logical partition does:

```sql
SELECT * FROM events
WHERE account_id % 8 = :partition
  AND sequence_id > :last_processed_id
ORDER BY sequence_id ASC
LIMIT :batch_size;
```

`ORDER BY sequence_id` provides the total ordering. But there's a subtle problem that Kafka doesn't have:

**The Visibility Gap Problem**

PostgreSQL sequences are **non-transactional** — a sequence value is assigned at INSERT time, not at COMMIT time. Two concurrent transactions can get sequences out of commit order:

```
Transaction A: gets sequence_id = 100, inserts event, still running...
Transaction B: gets sequence_id = 101, inserts event, COMMITS
Transaction A: still running...
```

A consumer querying `WHERE sequence_id > 99 ORDER BY sequence_id` sees sequence 101 but **not** 100 (invisible under MVCC — uncommitted). If it processes 101 and advances its cursor, sequence 100 will be **skipped** when Transaction A finally commits.

| Property | Kafka Offset | PostgreSQL Sequence |
|---|---|---|
| Assigned by | Partition leader (single writer) | Database (concurrent writers) |
| Gapless | Yes | No — gaps from rollbacks are normal |
| Order = commit order | Yes — always | No — sequence order can differ from commit order |
| Visibility | Immediate after append | Only after transaction commits (MVCC) |

**How Revolut mitigates this:**

1. **Polling with delay**: Consumers don't process events at the absolute head of the stream. A slight lag allows concurrent transactions to commit, closing most visibility gaps.
2. **Gap detection and retry**: The system detects sequence gaps and re-queries for missing IDs after a timeout.
3. **Transactional co-location**: Because Revolut writes the business state change and the event in the **same transaction** (dual-write pattern), events for a single account are typically serialized by business logic — the same account's events don't have concurrent writers in practice.
4. **Idempotent consumers**: If a gap-fill does deliver an event that was partially processed, idempotent processing handles it safely.

#### Redis Leases for Partition Assignment

Where Kafka uses a built-in Group Coordinator broker, Revolut uses **Redis-based distributed leases**:

```
Consumer tries to acquire:   SET partition:3:lock <consumer_id> NX EX 30
                              NX = only if not exists (atomic CAS)
                              EX 30 = expires in 30 seconds
While processing:             Periodically renew TTL (heartbeat)
On consumer crash:            Lease expires → another consumer acquires it
```

A **fencing token** (lease version or monotonic counter) prevents zombie consumers — a consumer that lost its lease but doesn't know it must present the fencing token when committing its cursor. Stale tokens are rejected. See [Martin Kleppmann — How to do distributed locking](https://martin-kleppmann.com/2016/02/08/how-to-do-distributed-locking.html) for why fencing tokens are essential.

| Aspect | Kafka Consumer Group | Revolut Redis Leases |
|---|---|---|
| Coordination | Built into broker (Group Coordinator) | External (Redis) |
| Assignment | Centralized algorithm (Range/Sticky) | Decentralized (each consumer grabs partitions) |
| Failure detection | Heartbeat + session timeout | Lease TTL expiration |
| Rebalancing | See below | Granular — only the failed consumer's partitions |
| Fencing | Group generation ID (epoch-based) | Fencing token / lease version |

Revolut's Redis lease model has an inherent advantage: when a consumer fails, only that consumer's partitions become available — other consumers continue uninterrupted. There is no coordination protocol, no group-wide event, no rebalance storm risk.

#### Kafka's Rebalancing: A Moving Target *(not Revolut-specific)*

Kafka's rebalancing has evolved significantly. Whether it's "stop-the-world" depends on which protocol you're using:

**Eager rebalancing (pre-Kafka 2.4, still the default in many deployments):** Genuinely stop-the-world. When any consumer joins or leaves, **all consumers revoke all partitions**, rejoin the group, and receive new assignments. The entire consumer group is idle during this process. From the [Confluent blog](https://www.confluent.io/blog/cooperative-rebalancing-in-kafka-streams-consumer-ksqldb/): "No member of the group can do any work for the duration of the rebalance."

**Cooperative incremental rebalancing ([KIP-429](https://cwiki.apache.org/confluence/display/KAFKA/KIP-429%3A+Kafka+Consumer+Incremental+Rebalance+Protocol), Kafka 2.4+, January 2020):** Only partitions that actually need to migrate are revoked. Partitions that stay with the same consumer **continue processing without interruption**. The Confluent benchmark showed eager protocol pause time of **37,138ms** vs. cooperative's **3,522ms** during a 10-instance rolling bounce. Requires opting in to `CooperativeStickyAssignor`.

**New consumer group protocol ([KIP-848](https://cwiki.apache.org/confluence/display/KAFKA/KIP-848%3A+The+Next+Generation+of+the+Consumer+Rebalance+Protocol), Kafka 3.7+, 2024):** Fundamentally redesigned. The broker performs assignment (no more "leader consumer"), consumers reconcile independently via heartbeats, no global synchronization barrier. Incremental by design — there is no eager mode. Still early access as of 2024.

| Era | Protocol | Stop-the-world? |
|---|---|---|
| Pre-2.4 (before 2020) | Eager (Range/RoundRobin) | **Yes** — all partitions revoked from all consumers |
| 2.4–3.6 (2020–2024) | Cooperative (CooperativeStickyAssignor) | **No** — only migrating partitions pause; requires opt-in |
| 3.7+ (2024+) | KIP-848 (server-side) | **No** — incremental by design |

Even with cooperative rebalancing, some pain points remain: rebalance storms during rolling deploys (mitigated by `group.initial.rebalance.delay.ms`), state store migration latency in Kafka Streams, and `max.poll.interval.ms` kicking out slow consumers. Revolut's Redis lease model sidesteps all of this — a lease expires, another consumer grabs it, done.

#### Ordering Guarantee Strength: Kafka vs Revolut

**Kafka's ordering is strictly stronger**: no visibility gaps, single writer eliminates ambiguity, offset = physical position. A consumer reading a partition sees records in the exact order they were appended, always.

**Revolut's ordering is weaker but more flexible**: sequence order can differ from commit order, and visibility gaps must be mitigated (see below). But logical partitioning means you can redefine partition keys, change partition counts, and apply different strategies per consumer — all without data migration or downtime.

#### How Visibility Gaps Are Mitigated in Practice

The core problem: PostgreSQL sequences are assigned at `INSERT` time (outside transaction isolation), but rows only become visible at `COMMIT` time. Two concurrent transactions can commit out of sequence order, making a later sequence visible before an earlier one.

**Approach 1: Delay buffer (simplest, most common — *not Revolut-specific, general practice*)**

The consumer intentionally reads events with a time delay, allowing concurrent transactions to commit before processing:

```sql
SELECT * FROM events
WHERE sequence_id > :last_processed_offset
  AND created_at < now() - interval '5 seconds'
ORDER BY sequence_id
LIMIT :batch_size;
```

Typical delay: 1–5 seconds for OLTP workloads, 10–30 seconds for systems with longer transactions. The delay must exceed the maximum expected transaction duration. Simple, robust, and handles permanent gaps from rollbacks naturally (rolled-back transactions consume sequence values that are never inserted — after the delay, if no row exists, it's a permanent gap and safely skipped).

**Approach 2: Transaction-boundary watermark using `xmin` (*not Revolut-specific, PostgreSQL technique*)**

Use PostgreSQL's `xmin` system column (the transaction ID that inserted the row) to only process rows whose transactions are definitely committed:

```sql
-- PostgreSQL 13+ syntax
SELECT e.* FROM events e
WHERE e.sequence_id > :last_processed_offset
  AND e.xmin::text::bigint < pg_snapshot_xmin(pg_current_snapshot())
ORDER BY e.sequence_id
LIMIT :batch_size;
```

`pg_snapshot_xmin(pg_current_snapshot())` returns the oldest still-active transaction ID. Any row with `xmin` below this is guaranteed committed (or aborted and invisible). No artificial delay needed, but the `xmin::text::bigint` cast is somewhat fragile and transaction ID wraparound must be considered.

**Approach 3: WAL-based CDC / Debezium (*not Revolut-specific — alternative architecture*)**

Avoids the problem entirely by reading the [Write-Ahead Log via logical replication](https://debezium.io/documentation/reference/stable/connectors/postgresql.html) instead of polling the table. The WAL streams changes **in commit order** — a transaction's changes only appear after it commits. No visibility gaps possible. Revolut doesn't use this because it would reintroduce Kafka as a dependency (Debezium outputs to Kafka topics).

**What Revolut likely uses:** Based on their published architecture, a combination of:
1. **Delay buffer** — a short polling lag to let concurrent transactions commit
2. **Transactional co-location** — because they write the business state and the event in the same transaction (dual-write pattern), events for the same entity are serialized by business logic, avoiding inter-transaction ordering issues within a partition
3. **Idempotent consumers** — even if a gap causes occasional reprocessing, idempotency makes it safe

Their emphasis on idempotency suggests they treat gaps as a "handle duplicates" problem rather than a "prevent gaps" problem — architecturally simpler and more robust.

The trade-off: Kafka gives you **ordering guarantees baked into the infrastructure** at the cost of operational complexity and partition rigidity. Revolut's approach gives you **flexibility** (one less system to run, dynamic repartitioning) at the cost of solving ordering edge cases at the application level.

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

Each layer catches a different failure mode:

| Failure Mode | Caught By |
|---|---|
| User double-clicks "Pay" | Layer 1 — API dedup (e.g., Redis with request hash, short TTL) |
| API gateway crashes mid-response, client retries | Layer 2 — Intent dedup (intent ID in durable store, survives restarts) |
| Message queue redelivers an event to the ledger | Layer 3 — Ledger idempotency key (UNIQUE constraint) |
| Bug creates two different intents for the same transfer | Layer 3 — Account balance constraint (insufficient funds blocks second debit) |
| External system already processed the transfer | Layer 4 — Partner's own dedup (e.g., Visa transaction ID) + reconciliation |

#### Deep Dive: Ledger Layer — Double-Entry Bookkeeping as Idempotency

Double-entry bookkeeping is not just an accounting principle — in a fintech system, it is a **correctness invariant enforced at the data layer**. The core rule: every movement of money produces at least two ledger entries (a debit and a credit), and the sum of all entries in a transaction must be **zero**. Money never appears from nowhere. As [TigerBeetle's documentation](https://docs.tigerbeetle.com/coding/financial-accounting/) puts it: "Money never just appears. Funds always go from somewhere to somewhere."

A typical fintech ledger entry looks like:

```sql
CREATE TABLE ledger_entries (
    id              UUID PRIMARY KEY,
    transaction_id  UUID NOT NULL,          -- groups the balanced debit+credit pair
    idempotency_key VARCHAR(255) UNIQUE,    -- derived from the intent ID above
    account_id      UUID NOT NULL,
    entry_type      VARCHAR(6) NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    amount          BIGINT NOT NULL CHECK (amount > 0),  -- always positive; direction from entry_type
    currency        VARCHAR(3) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
-- Invariant: SUM(CASE WHEN entry_type='DEBIT' THEN amount ELSE -amount END) = 0 per transaction_id
-- No UPDATE or DELETE — reversals are new entries
```

This gives **three layers of protection within the ledger itself**:

1. **Direct deduplication**: The `idempotency_key` (derived from the intent ID of layer 2) has a UNIQUE constraint. A duplicate event hits a UNIQUE VIOLATION and the entire transaction rolls back. The caller gets the same result as the original — indistinguishable from a first-time success.

2. **Structural safety**: The zero-sum invariant means partial replays cannot corrupt state. You can't write half a transaction — either the full balanced set of entries is written atomically, or nothing is. If a bug causes only the debit leg to be written and the credit leg to retry with a new transaction ID, the balance check blocks it.

3. **Economic safety net**: Account-level balance constraints (e.g., `balance >= 0` for user wallets) provide domain-level protection. Even if a duplicate somehow gets a new idempotency key (a bug), it may still fail because the account doesn't have sufficient funds for a second debit.

4. **Auditability as last resort**: Because the ledger is append-only and every entry is balanced, any anomaly that slips through all three mechanisms above is detectable during reconciliation — the books won't balance, and the discrepancy points to the exact problematic entries.

#### Example: Transfer $50 from Alice to Bob

```
Event arrives: TransferIntended{intent_id=intent-abc-123, from=alice, to=bob, amount=50}

BEGIN TRANSACTION;
  INSERT INTO ledger_entries (idempotency_key, account_id, entry_type, amount)
    VALUES ('intent-abc-123-D', alice_id, 'DEBIT', 5000);   -- 5000 cents
  INSERT INTO ledger_entries (idempotency_key, account_id, entry_type, amount)
    VALUES ('intent-abc-123-C', bob_id, 'CREDIT', 5000);
  VALIDATE: sum for this transaction = 0 ✓
  VALIDATE: alice.balance >= 0 ✓
COMMIT;

If the same event replays:
  INSERT fails → UNIQUE VIOLATION on 'intent-abc-123-D' → ROLLBACK → duplicate safely rejected
```

**Contrast with traditional accounting**: Traditional double-entry detects imbalances during end-of-day/month reconciliation by a human. Fintech ledgers enforce it as a **real-time database constraint** on the write path — the transaction is rejected before it ever persists.

For more on this pattern, see:
- [Brandur Leach — Implementing Stripe-like Idempotency Keys in Postgres](https://brandur.org/idempotency-keys): the definitive reference on carrying idempotency keys through a transactional system with atomic phases and recovery points
- [Modern Treasury — Accounting for Developers](https://www.moderntreasury.com/journal/accounting-for-developers-part-i): explains double-entry implementation in software — "it's more accurate to store immutable transactions and always compute balances from those transactions"
- [Stripe — Designing Robust APIs with Idempotency](https://stripe.com/blog/idempotency): how Stripe uses `Idempotency-Key` headers from API layer down to payment processing
- [TigerBeetle — Financial Accounting](https://docs.tigerbeetle.com/coding/financial-accounting/): a purpose-built double-entry ledger database with built-in idempotency and balance invariants

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
- [PostgreSQL NOTIFY documentation](https://www.postgresql.org/docs/current/sql-notify.html)
- [PostgreSQL LISTEN documentation](https://www.postgresql.org/docs/current/sql-listen.html)
- [Brandur Leach — Implementing Stripe-like Idempotency Keys in Postgres](https://brandur.org/idempotency-keys)
- [Modern Treasury — Accounting for Developers](https://www.moderntreasury.com/journal/accounting-for-developers-part-i)
- [Stripe — Designing Robust APIs with Idempotency](https://stripe.com/blog/idempotency)
- [TigerBeetle — Financial Accounting](https://docs.tigerbeetle.com/coding/financial-accounting/)
- [Jay Kreps — The Log: What every software engineer should know about real-time data's unifying abstraction](https://engineering.linkedin.com/distributed-systems/log-what-every-software-engineer-should-know-about-real-time-datas-unifying)
- [Apache Kafka — Introduction and Design](https://kafka.apache.org/intro)
- [Martin Kleppmann — How to do distributed locking](https://martin-kleppmann.com/2016/02/08/how-to-do-distributed-locking.html)
- [PostgreSQL — MVCC and Transaction Isolation](https://www.postgresql.org/docs/current/mvcc.html)
- [Confluent — Cooperative Rebalancing in Kafka Streams, Consumer, and ksqlDB](https://www.confluent.io/blog/cooperative-rebalancing-in-kafka-streams-consumer-ksqldb/)
- [KIP-429 — Kafka Consumer Incremental Rebalance Protocol](https://cwiki.apache.org/confluence/display/KAFKA/KIP-429%3A+Kafka+Consumer+Incremental+Rebalance+Protocol)
- [KIP-848 — The Next Generation of the Consumer Rebalance Protocol](https://cwiki.apache.org/confluence/display/KAFKA/KIP-848%3A+The+Next+Generation+of+the+Consumer+Rebalance+Protocol)
- [Debezium — PostgreSQL Connector (WAL-based CDC)](https://debezium.io/documentation/reference/stable/connectors/postgresql.html)
- [Redis — Persistence (RDB and AOF)](https://redis.io/docs/latest/operate/oss_and_stack/management/persistence/)
- [Kafka — Consumer Configuration (auto.offset.reset)](https://kafka.apache.org/documentation/#consumerconfigs_auto.offset.reset)
