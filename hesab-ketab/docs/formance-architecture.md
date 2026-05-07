# Formance Ledger: Open Source Financial Ledger on PostgreSQL

## The 30-Second Version

Formance Ledger is an open-source, Go-based financial ledger backed by PostgreSQL. It uses an immutable, hash-chained append-only log as the source of truth. Transactions are expressed in a DSL called Numscript. Concurrency is handled via row-level locks on (account, asset) pairs. It achieves ~1,000 writes/second per ledger and scales horizontally by sharding into multiple ledgers.

---

## Overview

Formance (formerly Numary, Y Combinator backed) is the closest open-source equivalent to what hesab-ketab is building. It's a standalone REST microservice that provides atomic, multi-posting transactions for money-moving applications: user balance platforms, digital asset exchanges, payment systems, loan management.

### Core Design Decisions

- **PostgreSQL only** — no Kafka, no Redis, no external dependencies beyond Postgres
- **Immutable append-only log** — the transaction log is the source of truth, balances are derived
- **Hash-chaining** — each transaction hash = SHA(transaction data + previous hash), creating a tamper-evident chain
- **Integer-only arithmetic** — no floating-point math anywhere, ever

---

## Transaction Model: Source-Destination

Formance uses a **source-destination model** instead of classical debit/credit double-entry:

```json
{
  "postings": [
    {
      "source": "bank:main",
      "destination": "users:alice",
      "asset": "USD/2",
      "amount": 100
    }
  ]
}
```

Key properties:
- **Atomic**: All postings in a transaction succeed or fail as one unit
- **Multi-posting**: A single transaction can move money between many accounts
- **Conservation enforced**: Sum of sources must equal sum of destinations
- **No negative balances**: Accounts cannot go below zero (except the special `@world` account for money creation)

### Asset Notation

Currency includes decimal precision: `USD/2` means US dollars with 2 decimal places. `BTC/8` means Bitcoin with 8. This eliminates ambiguity and prevents precision errors.

### Account Addressing

Hierarchical namespace: `users:1234:main`, `platform:fees:collateral`. Supports wildcard queries like `users:1234:*` for efficient lookups.

**Hesab-Ketab Comparison**: Your model uses `AccountEvent.MoneyDebited` and `AccountEvent.MoneyCredited` as separate event types. Formance unifies these into a single posting with source/destination. Both enforce conservation — yours through the `Account.apply()` method, theirs through the posting model itself.

---

## Numscript: Transaction DSL

Instead of writing transaction logic in application code, Formance uses a declarative DSL:

```numscript
send [USD/2 599] (
  source = @bank:main
  destination = @users:alice
)
```

### Why a DSL?

- **Readable by non-engineers**: Compliance and finance teams can audit transaction logic
- **Prevents rounding errors**: Integer-only, deterministic rounding rules built in
- **Deterministic**: Same inputs always produce same outputs
- **Auditable**: The script itself is part of the audit trail

### Advanced Features

**Percentage-based splits:**
```numscript
send [USD/2 100] (
  source = @bank:main
  destination = {
    85% to @users:alice
    remaining to @platform:fees
  }
)
```

The `remaining` keyword prevents rounding errors — whatever's left after the 85% split goes to fees. No lost cents.

**Hesab-Ketab Comparison**: You don't have a DSL — transaction logic lives in Java code (`Account.apply()`). For a learning project, this is fine. The DSL approach shines when you need non-engineers to define and audit financial rules.

---

## Database Architecture

### Storage Design

```
PostgreSQL
  |
  |-- Bucket (separate schema per bucket)
  |     |-- Ledger A
  |     |     |-- Transactions (immutable, hash-chained)
  |     |     |-- Accounts (derived balances)
  |     |
  |     |-- Ledger B
  |           |-- ...
  |
  |-- Bucket 2 (separate schema)
        |-- ...
```

**Bucket primitive**: Each bucket maps to a separate PostgreSQL schema. This provides data isolation at the storage layer and lays the foundation for horizontal sharding.

### Immutable Transaction Log

The log is the source of truth. Balances are derived from it.

**Hash-chaining**: Each transaction produces:
```
hash_n = SHA256(transaction_data_n + hash_{n-1})
```

Any modification to any historical transaction cascades hash failures through all subsequent transactions. Tampering is immediately detectable.

**Hesab-Ketab Comparison**: Your `domain_events` table is also append-only and immutable, but without hash-chaining. Adding hash-chaining would give you tamper evidence — each event's hash depends on the previous, forming an auditable chain. The `aggregate_version` unique constraint partially serves this purpose (prevents gaps) but doesn't detect modification of existing events.

---

## Consistency Model

### Six Core Invariants

1. **Conservation**: Every debit has an equal credit. Corrections via compensating entries only — never silent modification.
2. **Uniqueness**: Idempotency keys with deduplication windows. Duplicate submissions are safely ignored.
3. **Ordering**: Deterministic transaction sequencing. Same inputs always yield same final balances.
4. **Reference Integrity**: All transactions reference valid accounts with correct currency precision.
5. **Reproducibility**: Any balance can be derived by replaying the transaction log. Zero drift between computed and recorded.
6. **Ownership Attribution**: Every unit of value traceable to beneficial owner via metadata.

### Concurrency Control

**Row-level locks on (account, asset) pairs.** When two transactions target the same source account concurrently, one waits for the other.

```
Transaction A: debit users:alice USD/2  --> acquires lock on (users:alice, USD/2)
Transaction B: debit users:alice USD/2  --> waits for lock
Transaction A: commits                  --> releases lock
Transaction B: acquires lock, proceeds
```

This is the primary throughput bottleneck. Under contention (many transactions for the same account), writes serialize.

**Hesab-Ketab Comparison**: Your event store uses `UNIQUE (aggregate_id, aggregate_version)` as an optimistic concurrency control. If two transactions try to write the same version for the same aggregate, one fails with a unique constraint violation. Formance uses pessimistic locking (row locks); you use optimistic locking (version conflicts). Both serialize writes per aggregate — the question is whether you fail fast (optimistic) or wait (pessimistic).

---

## Scaling Strategy

### Baseline Performance

~1,000 writes per second per ledger. Single-writer, sequential writes.

### Reducing Contention

**Strategy 1: Distribute source accounts**

Instead of one `@bank:main` account as the source for all payouts:
```
@world:pool-01
@world:pool-02
...
@world:pool-20
```

~20 pool accounts balance performance against proliferation. Each pool has its own row lock, so 20 pools = 20x less contention.

**Strategy 2: Async hash computation**

The hash chain requires sequential computation (hash N depends on hash N-1). Making this asynchronous removes it from the critical write path. Trade-off: you temporarily lose cryptographic proof until the async worker catches up.

### Horizontal Scaling: Multi-Ledger

```
Ledger per user cohort:
  ledger-users-0001-to-1000
  ledger-users-1001-to-2000
  ...

Or ledger per time period:
  ledger-2024
  ledger-2025
```

Each ledger is a separate PostgreSQL schema (via the bucket primitive). No cross-ledger transactions, no shared locks. Scales linearly.

**Hesab-Ketab Comparison**: Your Kafka-style partitioning doc describes a similar approach — partition aggregates across pods by hash. Formance partitions at a higher level (entire ledgers), which is coarser but simpler. Your approach allows finer-grained scaling within a single event store.

---

## V2: Key Architectural Changes

### Bi-Temporality

Two timestamps per transaction:
- **Request time**: When the transaction reaches the system
- **Transaction time**: When the transaction logically occurred (effective/booking date)

This enables:
- Backdated corrections without rewriting history
- Fixed-point-in-time auditing for compliance
- Historical state analysis without reverse-engineering
- Data migration with preserved original timestamps

**Hesab-Ketab Comparison**: Your `domain_events` table has `created_at` (request time). Your `loadEventsUpTo(aggregateId, asOf)` query supports temporal queries. Adding a separate `effective_at` column would give you bi-temporality — the ability to distinguish "when did this happen?" from "when did we learn about it?"

### Process Separation

V2 splits into two processes:
- `ledger serve`: Handles HTTP requests (stateless, horizontally scalable)
- `ledger worker`: Manages hash computation and log exports (stateful, single-writer)

### Improved Querying

JSON-based filtering with logical operators:
```json
{
  "$and": [
    {"metadata.customer_id": "12345"},
    {"amount": {"$gte": 1000}}
  ]
}
```

---

## Event Publishing

Formance supports two event delivery mechanisms:

### Webhook (HTTP)

```
PUBLISHER_HTTP_ENABLED=true
PUBLISHER_TOPIC_MAPPING='COMMITTED_TRANSACTIONS:https://your-service/webhook'
```

### Kafka

```
PUBLISHER_KAFKA_ENABLED=true
PUBLISHER_KAFKA_BROKER=kafka:9092
PUBLISHER_TOPIC_MAPPING='COMMITTED_TRANSACTIONS:transactions-topic'
```

### Event Types

- `COMMITTED_TRANSACTIONS` — new transaction posted
- `SAVED_METADATA` — metadata updates
- `REVERTED_TRANSACTION` — transaction reversals

**Hesab-Ketab Comparison**: Your PostgreSQL LISTEN/NOTIFY serves the same purpose as Formance's event publishing — notifying external consumers of new events. Formance supports Kafka for durable, scalable delivery; your approach keeps everything in Postgres.

---

## Error Handling

### Insufficient Funds

Hard constraint: accounts cannot go negative (except `@world`). The transaction fails atomically — no partial execution.

### Corrections

Never modify history. Always add a compensating entry:

```
Original:  bank:main -> users:alice  $100
Reversal:  users:alice -> bank:main  $100
```

Full lineage preserved. Both entries visible in the audit trail.

**Hesab-Ketab Comparison**: Your `Account.apply()` returns `SuccessOrFailure` with a reason. The consumer logs the failure and advances the offset. Formance rejects the entire transaction at the API level. Your approach is event-sourced (failure is recorded as a processed event); theirs is synchronous (failure prevents the transaction from being created).

---

## Architecture Summary

```
Client (REST API)
  |
  v
Ledger Service (Go, stateless)
  |
  |-- Numscript parser/compiler
  |-- Transaction validator (conservation, balance checks)
  |-- Row lock acquisition on (account, asset)
  |
  v
PostgreSQL
  |-- Bucket schema isolation
  |-- Immutable transaction log (hash-chained)
  |-- Account balances (derived state)
  |
  v
Event Publisher
  |-- Webhook (HTTP)
  |-- Kafka
  v
External Consumers
```

## Sources

- [GitHub — formancehq/ledger](https://github.com/formancehq/ledger)
- [Formance Ledger Documentation](https://docs.formance.com/modules/ledger)
- [Announcing Formance Ledger V2](https://www.formance.com/blog/product/announcing-formance-ledger-v2)
- [Defining Double Entry — Formance Engineering](https://www.formance.com/blog/engineering/defining-double-entry)
- [How Not to Build a Ledger — Formance Engineering](https://www.formance.com/blog/engineering/how-not-to-build-a-ledger)
- [Architecting for Scale — Formance Documentation](https://docs.formance.com/modules/ledger/advanced-topics/architecting-for-scale)
- [Numscript Documentation](https://docs.formance.com/modules/numscript/introduction)
