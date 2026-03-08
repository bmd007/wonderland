# event-bus-support

### What is new in 3.x?
The subscriber will no longer error logs but instead warn log when it fails to process a message on the subscription.
The exception that already existed for subscriptions with dlq's still remain but it will info log until the last attempt fails before sending to DLQ and then it error logs.

### What is new in 2.x?
Added possibility to create a subscriber with a callback if the connection is broken.
This is an opt in feature and is not enabled by default.
To enable it you have to create the subscriber with an ApplicationEventPublisher, this is available by default in spring boot applications.

### What's new in 1.x?
In version 1.0 a new [PersistedPublisher](src/main/java/io/github/pubsubseekbucket/publish/persisted/PersistedPublisher.java) was introduced, intended as a replacement for [DatabaseSyncedPublisher](src/main/java/io/github/pubsubseekbucket/publish/databasesyncedpublisher/DatabaseSyncedPublisher.java).

This was motivated by a need to support time-series style events with non-uuid identifiers and potentially external sequence numbers (int8). Switching requires migrating to a new database table for outbox.

The new implementation also supports retaining custom PubSub headers provided in original message, if resent via [OutboxDrainer](src/main/java/io/github/pubsubseekbucket/publish/persisted/OutboxDrainer.java).

#### Migration notes
To guarantee delivery of any pending messages in old event_outbox a responsible migration involves deploying an interim version where old [EventOutboxDrainer](src/main/java/io/github/pubsubseekbucket/publish/databasesyncedpublisher/EventOutboxDrainer.java)(s) are still active.
Once publishing using the new Outbox functionality is fully rolled out and the old event_outbox is drained, the old Drainer can be safely removed.
Verification of Outbox queue can be done via e.g. [Prometheus metrics](https://ops.prod.nntech.io/prometheus/graph?g0.expr=event_outbox_size%7Bnamespace%3D%22instrument%22%7D&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h).

High-level sequence to consider when migrating:
1. To persist pending outgoing events, call appropriate PersistedPublisher#persistEvent(..) method.
2. Complete related entity operations in the transactional scope (e.g. updating both Instruments when moving a CustodyInstrument between Instrument A and B.)
3. Commit the transaction scope. At this point, eventual delivery is still guaranteed from the [OutboxDrainer](src/main/java/io/github/pubsubseekbucket/publish/persisted/OutboxDrainer.java).
4. Call [publish](src/main/java/io/github/pubsubseekbucket/publish/persisted/PublishTrigger.java) to trigger immediate PubSub delivery (and removal from Outbox on successful handover). 

## Points to consider when you are going to use this library 

* How to create SubscriptionAdminUtil bean to Subscribe and Create snapshots in wolf apps

On prem 

```
@Import(EventBusConfigurationOnPrem.class)
```

Nnx 

```
@Import(EventBusConfigurationNnx.class)
```

## Option to not delete dynamic subscriptions
Setting property as below will disable the feature to remove dynamic subscriptions upon application shutdown.
Useful in e2e test etc, not to be used in production.
```
subscription.deleter.active: false
```
