package wonderland.api.gateway.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.regex.Pattern;

import static org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_COMPACT;
import static org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_CONFIG;
import static org.apache.kafka.common.config.TopicConfig.RETENTION_MS_CONFIG;

/**
 * Configuration class to automatically create the topics with the configured partitions and replication factor.
 */
@Configuration
@Profile("!test")
public class TopicCreator {

    private final PartitionDef eventsTopicDefinition;
    private final PartitionDef updateTopicDefinition;
    private final String applicationName;

    public TopicCreator(
            @Value("${spring.application.name}") String applicationName,
            @Value("${kafka.topic.config.event}") String eventsTopicDefinition,
            @Value("${kafka.topic.config.update}") String updateTopicDefinition) {
        this.applicationName = applicationName;
        this.eventsTopicDefinition = PartitionDef.parse(eventsTopicDefinition);
        this.updateTopicDefinition = PartitionDef.parse(updateTopicDefinition);

    }

    @Bean
    public NewTopic dancePartnerEvents() {
        return new NewTopic(Topics.DANCE_PARTNER_EVENTS, eventsTopicDefinition.numPartitions, eventsTopicDefinition.replicationFactor)
                .configs(Map.of(RETENTION_MS_CONFIG, "-1", TopicConfig.RETENTION_BYTES_CONFIG, "-1"));
    }

    @Bean
    public NewTopic dancerSeekungPartnerUpdates() {
        return new NewTopic(Topics.DANCE_PARTNER_EVENTS, updateTopicDefinition.numPartitions, updateTopicDefinition.replicationFactor)
                .configs(Map.of(CLEANUP_POLICY_CONFIG, CLEANUP_POLICY_COMPACT));
    }

    private record PartitionDef(int numPartitions, short replicationFactor) {

        private final static Pattern PATTERN = Pattern.compile("(\\d+):(\\d+)");

        public static PartitionDef parse(String value) {
            var matcher = PATTERN.matcher(value);
            if (matcher.matches()) {
                var numParts = Integer.parseInt(matcher.group(1));
                var repFactor = Short.parseShort(matcher.group(2));
                return new PartitionDef(numParts, repFactor);
            } else {
                throw new IllegalArgumentException("Invalid topic partition definition: " + value);
            }
        }
    }
}
