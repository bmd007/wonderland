package io.github.pubsubseekbucket.subscribe.integrationtest;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import io.github.pubsubseekbucket.publish.PublisherFactory;
import io.github.pubsubseekbucket.publish.persisted.PersistedPublisher;
import io.github.pubsubseekbucket.util.SubscriptionAdminUtil;

import java.util.Map;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    private static final String INSTRUMENT_BUCKET_NAME = "bucketName";

    @Autowired
    SubscriptionAdminUtil subscriptionAdminUtil;

    @Value("${embedded.google.pubsub.topics-and-subscriptions[0].subscription}")
    private String subscriptionId;

    @Autowired
    private PublisherFactory publisherFactory;

    @Primary
    @Bean
    public Storage createStorage() {
        return LocalStorageHelper.getOptions().getService();
    }

    @Primary
    @Bean
    public ApplicationEventPublisher applicationEventPublisher() {
        return mock(ApplicationEventPublisher.class);
    }

    @Bean
    public PersistedPublisher<String> testPublisher() {
        return publisherFactory.createPersistedPublisher("test-topic", event -> event, event -> 1L);
    }

    private void createSnapshot(String dumpName) {
        subscriptionAdminUtil.createSnapshot(subscriptionId, dumpName, Map.of());
    }
}
