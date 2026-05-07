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

When pods scale up or down, the partition assignment changes. This is the hardest part of the design.

### The Problem with Modulo

With `hash % podCount`, scaling from 3 to 4 pods reshuffles nearly **100% of assignments**. An aggregate that was `hash % 3 = 1` (pod 1) might become `hash % 4 = 2` (pod 2). Almost every aggregate moves to a different pod — triggering mass reprocessing from each pod's last known offset.

During the transition window:
- The old pod may still be processing events for aggregates it's about to lose
- The new pod starts processing from its offset, potentially re-applying events
- Two pods briefly own the same aggregate — exactly what partitioning was supposed to prevent

### Approach 1: Consistent Hashing

Instead of `hash % podCount`, place pods and aggregates on a hash ring. Each aggregate maps to the nearest pod clockwise.

When a pod is added, it only claims aggregates in its arc of the ring — roughly `1/N` of the total. The other `(N-1)/N` stay with their current owner. Scaling from 3 to 4 pods moves ~25% of aggregates instead of ~100%.

**Trade-off**: More complex to implement (hash ring, virtual nodes for even distribution), but dramatically reduces reprocessing on scale events.

### Approach 2: Lease-Based (Revolut's Approach)

Pods claim partitions by acquiring Kubernetes Leases or Redis locks. A partition is owned by whoever holds the lease. No static assignment — pods grab available partitions on startup.

```
Pod starts → scans for unclaimed partitions → acquires lease → processes events
Pod dies → lease expires (15-60s) → another pod claims it
Scale up → new pod claims free partitions, no existing assignments change
Scale down → evicted pod's leases expire, remaining pods pick them up
```

**Trade-off**: Requires a coordination layer (Kubernetes Lease API or Redis). Temporary processing gap while leases expire. But zero reshuffling — existing assignments never change.

### Approach 3: Custom CRD Controller

Define a `PartitionAssignment` custom resource. A controller watches pod lifecycle and reconciles assignments:

```yaml
apiVersion: hesab-ketab.io/v1
kind: PartitionAssignment
spec:
  partitions:
    - index: 0
      owner: consumer-0
    - index: 1
      owner: consumer-1
```

The controller decides the assignment strategy (balanced, weighted, affinity-aware). Pods watch the CRD and process only their assigned partitions.

**Trade-off**: Highest engineering effort, but full declarative control. GitOps-friendly, auditable, supports complex rules.

### Comparison

| Aspect | Modulo | Consistent Hash | Lease-Based | Custom CRD |
|--------|--------|-----------------|-------------|------------|
| Reshuffling on scale | ~100% | ~1/N | 0% | Tunable |
| Duplicate risk | High | Low | Very low | None |
| Complexity | Low | Medium | Medium | High |
| K8s-native | No | No | Yes (Leases) | Yes (CRD) |

### Recommendation

Start with **modulo** (current design). When scaling becomes a real requirement, move to **consistent hashing** — it's the best balance of complexity vs. correctness. Lease-based is worth it if you need zero-downtime scaling and already use Redis or Kubernetes Leases.

### Idempotency Requirement

All approaches require **idempotent event application** — applying the same event twice must produce the same result. The current `AccountRepository.save()` already handles this with `WHERE accounts.version = :version - 1`, which rejects stale or duplicate applies.

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
