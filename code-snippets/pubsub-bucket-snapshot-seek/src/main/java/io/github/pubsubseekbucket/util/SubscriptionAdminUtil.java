package io.github.pubsubseekbucket.util;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.NotFoundException;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.protobuf.Timestamp;
import com.google.pubsub.v1.CreateSnapshotRequest;
import com.google.pubsub.v1.DeadLetterPolicy;
import com.google.pubsub.v1.ExpirationPolicy;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.ProjectSnapshotName;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.RetryPolicy;
import com.google.pubsub.v1.SeekRequest;
import com.google.pubsub.v1.Snapshot;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.TopicName;
import lombok.extern.slf4j.Slf4j;
import io.github.pubsubseekbucket.exception.EventBusSupportCommunicationException;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class SubscriptionAdminUtil {
    public static final SubscriptionConfiguration DEFAULT_CONFIGURATION = new SubscriptionConfiguration();
    private final String pubsubProject;

    private final CredentialsProvider credentialsProvider;

    private final TransportChannelProvider transportChannelProvider;

    private final Collection<Subscription> dynamicSubscriptions = new ArrayList<>();

    public SubscriptionAdminUtil(String projectId,
                                 CredentialsProvider credentialsProvider,
                                 TransportChannelProvider transportChannelProvider) {
        this.pubsubProject = projectId;
        this.credentialsProvider = credentialsProvider;
        this.transportChannelProvider = transportChannelProvider;
    }

    @Deprecated(forRemoval = true)
    public Subscription createSubscription(String topicId, String subscriptionId) {
        return doCreateSubscription(topicId, null, null, subscriptionId, DEFAULT_CONFIGURATION);
    }

    public Subscription createSubscription(String topicId, String subscriptionId, String team) {
        return doCreateSubscription(topicId, null, team, subscriptionId, DEFAULT_CONFIGURATION);
    }

    @Deprecated(forRemoval = true)
    public Subscription createDynamicSubscription(String topicId, String service) {
        return doCreateDynamicSubscription(topicId, service, null, DEFAULT_CONFIGURATION);
    }

    public Subscription createDynamicSubscription(String topicId, String service, String team) {
        return createDynamicSubscription(topicId, service, team, DEFAULT_CONFIGURATION);
    }

    public Subscription createDynamicSubscription(String topicId, String service, String team, SubscriptionConfiguration configuration) {
        if (service == null)
            throw new IllegalArgumentException("Service must not be null");
        if (team == null)
            throw new IllegalArgumentException("Team must not be null");

        return doCreateDynamicSubscription(topicId, service, team, configuration);
    }

    private Subscription doCreateDynamicSubscription(String topicId, String service, String team, SubscriptionConfiguration configuration) {
        var subscriptionId = topicId + "_" + service + "_" + UUID.randomUUID();

        Subscription subscription = doCreateSubscription(topicId, service, team, subscriptionId, configuration);

        log.info("Created dynamic subscription {}", subscriptionId);

        synchronized (dynamicSubscriptions) {
            dynamicSubscriptions.add(subscription);
        }

        return subscription;
    }

    private Subscription doCreateSubscription(String topicId, String service, String team, String subscriptionId, SubscriptionConfiguration configuration) {
        try (SubscriptionAdminClient subscriptionAdminClient = getSubscriptionAdminClient()) {
            Subscription.Builder subscriptionBuilder = Subscription.newBuilder()
                    .setName(ProjectSubscriptionName.of(pubsubProject, subscriptionId).toString())
                    .setTopic(TopicName.of(pubsubProject, topicId).toString())
                    .putLabels("dynamic", "true");

            if (service != null)
                subscriptionBuilder.putLabels("service", service);
            if (team != null)
                subscriptionBuilder.putLabels("team", team);

            if (configuration.getMaxBackoff() != null || configuration.getMinBackoff() != null) {
                subscriptionBuilder.setRetryPolicy(getRetryPolicy(configuration));
            }
            if (configuration.getDeadLetterTopic() != null || configuration.getMaxDeliveryAttempts() != null) {
                subscriptionBuilder.setDeadLetterPolicy(getDeadLetterPolicy(configuration));
            }
            if (configuration.getAckDeadline() != null) {
                subscriptionBuilder.setAckDeadlineSeconds((int) configuration.getAckDeadline().getSeconds());
            }
            if (configuration.getExpiry() != null) {
                subscriptionBuilder.setExpirationPolicy(getExpirationPolicy(configuration));
            }
            return subscriptionAdminClient.createSubscription(subscriptionBuilder.build());
        } catch (IOException e) {
            log.error("Failed creating subscription {}", subscriptionId, e);
            throw new EventBusSupportCommunicationException("Error creating subscription for topic " + topicId + " and id " + subscriptionId, e);
        }
    }

    private static ExpirationPolicy getExpirationPolicy(SubscriptionConfiguration configuration) {
        return ExpirationPolicy.newBuilder()
                .setTtl(convertDuration(configuration.getExpiry()))
                .build();
    }

    private static DeadLetterPolicy getDeadLetterPolicy(SubscriptionConfiguration configuration) {
        DeadLetterPolicy.Builder builder = DeadLetterPolicy.newBuilder();
        if (configuration.getDeadLetterTopic() != null)
            builder.setDeadLetterTopic(configuration.getDeadLetterTopic());
        if (configuration.getMaxDeliveryAttempts() != null)
            builder.setMaxDeliveryAttempts(configuration.getMaxDeliveryAttempts());
        return builder.build();
    }

    private static RetryPolicy getRetryPolicy(SubscriptionConfiguration configuration) {
        RetryPolicy.Builder builder = RetryPolicy.newBuilder();
        if (configuration.getMaxBackoff() != null)
            builder.setMaximumBackoff(convertDuration(configuration.getMaxBackoff()));
        if (configuration.getMinBackoff() != null)
            builder.setMinimumBackoff(convertDuration(configuration.getMinBackoff()));
        return builder.build();
    }

    private static com.google.protobuf.Duration convertDuration(Duration duration) {
        return com.google.protobuf.Duration.newBuilder()
                .setSeconds(duration.getSeconds())
                .setNanos(duration.getNano())
                .build();
    }

    void deleteDynamicSubscriptions() {
        synchronized (dynamicSubscriptions) {
            if (dynamicSubscriptions.isEmpty()) {
                log.debug("No dynamic subscriptions to delete, aborting.");
                return;
            }
            try (ExecutorService executorService = Executors.newFixedThreadPool(dynamicSubscriptions.size())) {
                CountDownLatch latch = new CountDownLatch(dynamicSubscriptions.size());
                dynamicSubscriptions.forEach(subscription ->
                        executorService.execute(() -> {

                            try (SubscriptionAdminClient subscriptionAdminClient = getSubscriptionAdminClient()) {
                                log.info("Deleting dynamic subscription {}", subscription.getName());
                                subscriptionAdminClient.deleteSubscription(subscription.getName());
                                log.info("Deleted dynamic subscription {}", subscription.getName());
                            } catch (IOException e) {
                                log.error("Failed deleting subscription {}", subscription.getName(), e);
                            }
                            latch.countDown();
                        }));

                try {
                    latch.await(20, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    log.error("Wait for futures to complete was interrupted", e);
                }

                if (latch.getCount() != 0) {
                    log.warn("Some futures did not complete within 20 seconds");
                }
                executorService.shutdownNow();
            }
        }
    }

    public Snapshot createSnapshot(Subscription subscription, String snapshotId, Map<String, String> snapshotLabels) {
        return createSnapshot(ProjectSubscriptionName.parse(subscription.getName()).getSubscription(), snapshotId, snapshotLabels);
    }

    public Snapshot createSnapshot(String subscriptionId, String snapshotId, Map<String, String> snapshotLabels) {
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(pubsubProject, subscriptionId);
        ProjectSnapshotName snapshotName = ProjectSnapshotName.of(pubsubProject, snapshotId);

        try (SubscriptionAdminClient subscriptionAdminClient = getSubscriptionAdminClient()) {
            CreateSnapshotRequest request = CreateSnapshotRequest.newBuilder()
                    .setSubscription(subscriptionName.toString())
                    .setName(snapshotName.toString())
                    .putAllLabels(snapshotLabels)
                    .build();

            return subscriptionAdminClient.createSnapshot(request);
        } catch (IOException e) {
            throw new EventBusSupportCommunicationException("Error creating subscription for subscription " + subscriptionId, e);
        }
    }

    private SubscriptionAdminClient getSubscriptionAdminClient() throws IOException {
        return SubscriptionAdminClient.create(
                SubscriptionAdminSettings.newBuilder()
                        .setTransportChannelProvider(transportChannelProvider)
                        .setCredentialsProvider(credentialsProvider)
                        .build());
    }

    public void deleteSubscription(String subscriptionId) {
        try (SubscriptionAdminClient subscriptionAdminClient = getSubscriptionAdminClient()) {
            subscriptionAdminClient.deleteSubscription(ProjectSubscriptionName.of(pubsubProject, subscriptionId).toString());
        } catch (IOException e) {
            throw new EventBusSupportCommunicationException("Error deleting subscription " + subscriptionId, e);
        }
    }

    public OptionalInt getMaxDeliveryAttempts(String subscriptionId) {
        try (SubscriptionAdminClient subscriptionAdminClient = getSubscriptionAdminClient()) {
            var subscription = subscriptionAdminClient.getSubscription(ProjectSubscriptionName.of(pubsubProject, subscriptionId));
            if (subscription != null) {
                if (subscription.getDeadLetterPolicy() == null) {
                    log.info("No dead letter policy found for {} {}", pubsubProject, subscriptionId);
                    return OptionalInt.empty();
                }
                var maxDeliveryAttempts = subscription.getDeadLetterPolicy().getMaxDeliveryAttempts();
                log.info("Found max delivery attempts for {} {}: {}", pubsubProject, subscriptionId, maxDeliveryAttempts);
                return OptionalInt.of(maxDeliveryAttempts);
            }
            log.warn("No subscription found when getting max delivery attempts for {} {}", pubsubProject, subscriptionId);
            return OptionalInt.empty();
        } catch (NotFoundException e) {
            log.warn("NotFoundException when getting max delivery attempts for {} {}", pubsubProject, subscriptionId);
            return OptionalInt.empty();
        } catch (IOException e) {
            log.error("Exception when getting max delivery attempts for {} {}", pubsubProject, subscriptionId, e);
            throw new EventBusSupportCommunicationException("Error getting max delivery attempts for subscription " + subscriptionId, e);
        }
    }

    public void seekToSnapshot(String subscriptionId, String snapshotId) {
        try (SubscriptionAdminClient adminClient = getSubscriptionAdminClient()) {
            adminClient.seek(SeekRequest.newBuilder()
                    .setSnapshot(snapshotId)
                    .setSubscription(ProjectSubscriptionName.of(pubsubProject, subscriptionId).toString())
                    .build());
        } catch (IOException e) {
            throw new EventBusSupportCommunicationException(String.format("Error seeking to snapshot %s on subscription %s", snapshotId, subscriptionId), e);
        }
    }

    public void seekToTimestamp(String subscriptionId, Instant timestamp) {
        try (SubscriptionAdminClient adminClient = getSubscriptionAdminClient()) {
            adminClient.seek(SeekRequest.newBuilder()
                    .setTime(Timestamp.newBuilder()
                            .setSeconds(timestamp.getEpochSecond())
                            .setNanos(timestamp.getNano())
                            .build())
                    .setSubscription(ProjectSubscriptionName.of(pubsubProject, subscriptionId).toString())
                    .build());
            log.info("Successfully seeked subscription {} to timestamp {}", subscriptionId, timestamp);
        } catch (IOException e) {
            throw new EventBusSupportCommunicationException(String.format("Error seeking to timestamp %s on subscription %s", timestamp.toString(), subscriptionId), e);
        }
    }

    public List<Snapshot> listSnapshots(String topicId) {
        ProjectName projectName = ProjectName.of(pubsubProject);
        TopicName topicName = TopicName.of(pubsubProject, topicId);
        try (SubscriptionAdminClient adminClient = getSubscriptionAdminClient()) {
            return StreamSupport.stream(adminClient.listSnapshots(projectName).iterateAll().spliterator(), false)
                    .filter(snapshot -> snapshot.getTopic().equals(topicName.toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new EventBusSupportCommunicationException("Error listing snapshots on topic " + topicId, e);
        }
    }

}
