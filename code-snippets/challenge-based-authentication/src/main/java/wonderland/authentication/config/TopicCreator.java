package wonderland.authentication.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.regex.Pattern;

import static org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_COMPACT;
import static org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_CONFIG;

/**
 * Configuration class to automatically create the topics with the configured partitions and replication factor.
 */
@Configuration
@Profile("!test")
public class TopicCreator {

    private final PartitionDef eventsTopicDefinition;
    private final PartitionDef changeLogTopicDefinition;
    private final String applicationName;

    public TopicCreator(
            @Value("${spring.application.name}") String applicationName,
            @Value("${kafka.topic.config.events}") String eventsTopicDefinition,
            @Value("${kafka.topic.config.changelog}") String changeLogTopicDefinition) {
        this.applicationName = applicationName;
        this.eventsTopicDefinition = PartitionDef.parse(eventsTopicDefinition);
        this.changeLogTopicDefinition = PartitionDef.parse(changeLogTopicDefinition);
    }

    @Bean
    public NewTopic eventLogTopic() {
        return new NewTopic(Topics.EVENT_LOG, changeLogTopicDefinition.numPartitions, changeLogTopicDefinition.replicationFactor)
                .configs(Map.of(CLEANUP_POLICY_CONFIG, CLEANUP_POLICY_COMPACT));
    }

    private static class PartitionDef {

        private final static Pattern PATTERN = Pattern.compile("(\\d+):(\\d+)");

        private final int numPartitions;
        private final short replicationFactor;

        private PartitionDef(int numPartitions, short replicationFactor) {
            this.numPartitions = numPartitions;
            this.replicationFactor = replicationFactor;
        }

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
