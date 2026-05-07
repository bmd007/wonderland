# Kafka-Style Aggregate Partitioning

## Problem

With multiple pods consuming from the same event store, all pods receive all events. This causes:
- Contention on the same aggregate rows
- Duplicate projection writes
- Row-level lock conflicts on the accounts table
- Wasted work that gets worse with more pods

## Solution

Partition aggregates across pods so each pod owns a deterministic subset. No two pods ever process events for the same aggregate.

## How It Works

Each pod knows two things:
- `POD_INDEX` — its ordinal (0, 1, 2, ...)
- `POD_COUNT` — total number of consumer pods

The event store query filters events by partition:

```sql
SELECT event_type, payload::text, sequence_number, aggregate_id
FROM domain_events
WHERE sequence_number > COALESCE(
    (SELECT last_sequence FROM event_consumer_offsets WHERE consumer_name = :name), 0
)
AND abs(hashtext(aggregate_id::text)) % :podCount = :podIndex
ORDER BY sequence_number
```

`hashtext` produces a stable integer hash of the aggregate ID. Modulo by pod count assigns each aggregate to exactly one pod.

## Deployment

Use a Kubernetes **StatefulSet** instead of a Deployment. StatefulSet gives each pod a stable ordinal hostname (`consumer-0`, `consumer-1`, etc.), which derives `POD_INDEX` and `POD_COUNT`.

```yaml
env:
  - name: POD_INDEX
    valueFrom:
      fieldRef:
        fieldPath: metadata.labels['apps.kubernetes.io/pod-index']
  - name: POD_COUNT
    value: "3"  # or read from StatefulSet replicas
```

## Offset Tracking

Each pod tracks its own offset independently. The consumer name already includes the pod ID (`account-projector-<podId>`), so each partition has its own row in `event_consumer_offsets`.

## Rebalancing

When pods scale up or down, the partition assignment changes (because `% podCount` changes). Some aggregates move to a different pod. The new owner reprocesses events from its last known offset.

This requires **idempotent event application** — applying the same event twice must produce the same result. For a ledger this means checking `aggregate_version` before applying, or using upserts for projections.

## Comparison to Kafka

| Concept | Kafka | This Approach |
|---------|-------|---------------|
| Partition key | message key | `aggregate_id` |
| Partition assignment | consumer group protocol | `hashtext % podCount` |
| Offset tracking | consumer group offsets | `event_consumer_offsets` table |
| Rebalancing | automatic | manual on scale up/down |
| Ordering guarantee | per-partition | per-aggregate (same thing) |
| Broker | Kafka cluster | Postgres `domain_events` table |

The key similarity: events for the same aggregate always go to the same consumer, guaranteeing ordered processing per aggregate without cross-pod coordination.

## When to Add This

This is not needed on day one. Start with a single consumer pod. Add partitioning when:
- Projection lag grows because one pod can't keep up
- You observe row-level lock contention on the accounts table
- You need horizontal scaling for event throughput
