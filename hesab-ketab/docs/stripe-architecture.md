# Stripe Ledger: Money Movement at 5 Billion Events/Day

## The 30-Second Version

Stripe's Ledger is an immutable, append-only log that tracks every financial event across their Global Payments and Treasury Network. It models money movement as deterministic state machines, enforces double-entry bookkeeping, and uses cryptographic hashing for tamper evidence. 5 billion events daily, 99.99% of dollar volume verified within 4 days. Stripe does not publicly disclose their database technology.

---

## Overview

Stripe operates in 185 countries, handling 135 currencies and payment methods. Their Ledger system is the source of truth for all money movement — payments, refunds, transfers, settlements, holds, releases.

Unlike Revolut and Formance (which publicly share technical implementation details), Stripe shares the conceptual architecture without revealing infrastructure choices. What they do share is how they think about the problem, which is arguably more valuable.

---

## Core Architecture: Three Pillars

### 1. Immutable Append-Only Log

All entries are permanent. No retroactive edits or deletions — only new entries. This creates an auditable history and prevents tampering.

Corrections are recorded as new entries that compensate for the original. The original entry remains visible forever. This is the same principle as hesab-ketab's `domain_events` table and Formance's hash-chained log.

### 2. State Machine Abstraction

This is Stripe's key innovation. They model all money movement as deterministic state machines:

```
Payment lifecycle:
  Created -> Authorized -> Captured -> Settled
                |
                v
            Cancelled

Settlement lifecycle:
  Account A -> Undeposited Account B -> Deposited Account B
```

Each state transition generates an event in the ledger. The state machine is the "contract" between producer systems — if system A says a payment moved from Authorized to Captured, system B must see the corresponding fund flow in the ledger.

**Why state machines?**

Producer systems at Stripe are owned by different teams, written in different languages, deployed independently. The state machine abstraction lets them reason about consistency across these disconnected systems. If system A recorded a +$11 debit and system B recorded a -$10 credit, the state machine detects the $1 mismatch.

**Hesab-Ketab Comparison**: Your `AccountEvent` sealed interface (MoneyDebited, MoneyCredited) is a simple form of this — each event type represents a state transition. Stripe's approach is more elaborate: they model the entire lifecycle of a payment as a graph of states, and each edge produces one or more ledger events. You could evolve toward this by modeling transaction lifecycles (Initiated -> Debited -> Credited -> Settled) rather than individual account-level events.

### 3. Double-Entry Bookkeeping

Every transaction creates balanced debit and credit entries. System-wide balances always reconcile to zero.

```
Finalizing an invoice:
  DEBIT  AccountsReceivable  $100
  CREDIT DeferredRevenue     $100
  
  Sum of all debits = Sum of all credits (always)
```

This provides a mathematical proof of correctness. If the system-wide balance is ever non-zero, something is wrong — and you know exactly how wrong it is.

**Hesab-Ketab Comparison**: Your event model records debits and credits as separate events on individual accounts. You don't enforce cross-account balance invariants at the event level. Adding a `transactionId` that links a debit event on account A to a credit event on account B (which you already have in your events) gives you the foundation for double-entry verification. A separate reconciliation consumer could verify that every transactionId has exactly one debit and one credit of equal amounts.

---

## Cryptographic Hashing

Transactions are cryptographically hashed and linked to previous ones:

```
hash_n = hash(transaction_data_n + hash_{n-1})
```

This forms a tamper-evident chain (similar to blockchain, but optimized for financial operations rather than distributed consensus). Any alteration to any historical record changes its hash, which cascades through all subsequent hashes.

Both Stripe and Formance use this pattern. Revolut does not mention it. Hesab-ketab does not implement it.

---

## Scale and Validation

### The Numbers

| Metric | Value |
|--------|-------|
| Events per day | 5 billion |
| Events per transaction | ~100 |
| Dollar volume verified (4 days) | 99.99% |
| Activity monitored/categorized | 99.999% |
| Explainability achieved | 99.9999% |
| Countries | 185 |
| Currencies | 135 |

~100 events per transaction is a key number. A single payment generates events across authorization, capture, settlement, fee calculation, currency conversion, partner reconciliation, and more. This is why event sourcing makes sense at Stripe's scale — each of these stages is handled by a different system, and the ledger stitches them together.

### Three Validation Metrics

**Clearing**: "What fraction of the ledger is appropriately zeroed out at steady state?" Find clearing accounts with non-zero balances — those represent unresolved fund flows.

**Timeliness**: Data delivery speed. Critical for monthly reporting and settlement windows. Accounts for region-specific delays (e.g., Brazil's uniquely long settlement times) using `effective_at` timestamps.

**Completeness**: Cross-system validation that all upstream data has been ingested. Automated anomaly detection surfaces missing transactions.

### Data Quality Platform (DQP)

Built on top of the ledger as a separate layer:
- Continuous monitoring of fund flow health
- Hierarchical automated alerting with proposed solutions
- Team-level quality metrics including financial impact
- Manual analysis tooling for the long-tail edge cases

This separation is important: the ledger is the immutable record, the DQP is the monitoring layer. They have different concerns, different update frequencies, and different teams.

**Hesab-Ketab Comparison**: You don't have a validation layer yet. The simplest starting point: a separate consumer that verifies every `transactionId` has matching debit/credit events with equal amounts. This consumer reads the same event stream but builds a different projection — a reconciliation report instead of account balances. This is exactly the "multiple projections from the same events" benefit of event sourcing.

---

## Failure Handling

### What Goes Wrong

At Stripe's scale, everything goes wrong:
- Malformed reports from banking partners
- Propagated errors from network partners
- Macroeconomic disruptions (currencies ceasing to exist, bank collapses)
- Off-by-one errors across distributed systems

### How They Handle It

1. **Proactive alerting**: DQP surfaces issues automatically with proposed solutions
2. **Immutable corrections**: New compensating entries, never modifying originals
3. **Hierarchical triage**: Automated for 99.999%, manual for the long tail
4. **Temporal awareness**: Region-specific settlement delays modeled via `effective_at` timestamps, not treated as errors

**Hesab-Ketab Comparison**: Your `catchUp()` logs warnings for failed events (insufficient funds) and advances the offset. This is the right approach — don't get stuck on a failed event. The next step would be writing failed events to a dead-letter projection for manual review, similar to Stripe's long-tail triage.

---

## Consistency Model

### Strong Consistency Where It Matters

- Double-entry bookkeeping provides mathematical proof (system-wide balance = 0)
- ACID guarantees maintained despite 5 billion daily events
- Cryptographic hashing detects tampering

### Eventual Consistency Where Acceptable

- Cross-system validation happens asynchronously (within 4 days for 99.99%)
- Settlement timing is region-dependent
- The DQP monitoring layer is eventually consistent by design

This is the pragmatic insight: **not everything needs to be strongly consistent at the same time.** The individual ledger entries are ACID. The cross-system reconciliation is eventual. The monitoring is best-effort. Each layer has appropriate consistency guarantees for its purpose.

---

## Key Lessons for Hesab-Ketab

### What You Already Have

- Immutable append-only event log (domain_events)
- Event-driven projections (EventConsumer -> Account balance)
- Temporal queries (loadEventsUpTo, loadEventsBetween)
- Idempotent projection updates (version-based upsert)

### What You Could Add (In Order of Value)

1. **Reconciliation consumer** — verify transactionId pairs balance. Separate projection, same event stream.
2. **Dead-letter projection** — store failed events for manual review instead of just logging.
3. **Hash-chaining** — add `previous_hash` column to domain_events. Tamper evidence.
4. **Double-entry verification** — system-wide balance check: sum of all debits must equal sum of all credits.
5. **Bi-temporality** — add `effective_at` alongside `created_at` for backdated corrections.

---

## Architecture Summary

```
Producer Systems (Payments, Treasury, Connect, etc.)
  |
  | State machine transitions generate events
  v
Immutable Append-Only Ledger
  |-- Cryptographic hash chain
  |-- Double-entry balance invariant
  |-- ~100 events per transaction
  |
  ├--> Data Quality Platform (monitoring layer)
  |      |-- Clearing validation
  |      |-- Timeliness checks
  |      |-- Completeness verification
  |      |-- Automated alerting + proposed solutions
  |
  └--> Operational Dashboards
         |-- 99.9999% explainability
         |-- Team-level financial impact metrics
         |-- Manual triage for long-tail
```

## Sources

- [Ledger: Stripe's system for tracking and validating money movement — Stripe Dev Blog](https://stripe.dev/blog/ledger-stripe-system-for-tracking-and-validating-money-movement)
- [How Stripe Ensures Financial Accuracy with Ledger — Density Labs](https://densitylabs.io/blog/building-trust-how-stripe-ensures-financial-accuracy-with-ledger)
- [Immutable Audit Trails: How Stripe Prevents Fraud — HubiFi](https://www.hubifi.com/blog/immutable-data-stripe)
