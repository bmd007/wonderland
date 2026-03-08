package io.github.pubsubseekbucket.publish.persisted;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import io.github.pubsubseekbucket.publish.PublisherFactory;

import java.util.List;

@Configuration
public class PersistedPublisherConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "eventbus.publish.outbox", name = "databaseManager", havingValue = "postgres", matchIfMissing = true)
    OutboxDao postgresOutboxDao(NamedParameterJdbcTemplate jdbcTemplate) {
        return new PostgresOutboxDao(jdbcTemplate);
    }

    @Bean
    @ConditionalOnProperty(prefix = "eventbus.publish.outbox", name = "databaseManager", havingValue = "oracle")
    OutboxDao oracleOutboxDao(NamedParameterJdbcTemplate jdbcTemplate, @Value("${eventbus.publish.outbox.schema}") String outboxSchema) {
        return new OracleOutboxDao(jdbcTemplate, outboxSchema);
    }

    @Bean
    OutboxDrainer outboxDrainer(OutboxDao outboxDao,
                                List<PersistedPublisher<?>> persistedPublishers,
                                @Value("${eventbus.publish.drainFrequencyInSeconds:10}") int drainFrequencyInSeconds,
                                @Value("${eventbus.publish.drainLimit:10000}") long drainLimit,
                                @Value("${eventbus.publish.outbox.permitRandomOrder:false}") boolean permitRandomOrder,
                                @Value("${eventbus.publish.republishThresholdInSeconds:5}") int republishThresholdInSeconds,
                                MeterRegistry meterRegistry,
                                PublisherFactory publisherFactory) {
        return new OutboxDrainer(outboxDao, publisherFactory, persistedPublishers, drainFrequencyInSeconds, drainLimit, permitRandomOrder, republishThresholdInSeconds, meterRegistry);
    }
}
