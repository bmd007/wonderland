package wonderland.wonder.matcher.config;

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

    private final PartitionDef eventTopicDefinition;
    private final PartitionDef changeLogTopicDefinition;
    private final String applicationName;

    public TopicCreator(
            @Value("${spring.application.name}") String applicationName,
            @Value("${kafka.topic.config.event}") String eventTopicDefinition,
            @Value("${kafka.topic.config.changelog}") String changeLogTopicDefinition) {
        this.applicationName = applicationName;
        this.eventTopicDefinition = PartitionDef.parse(eventTopicDefinition);
        this.changeLogTopicDefinition = PartitionDef.parse(changeLogTopicDefinition);

    }

//    @Bean
//    public NewTopic eventsTopic() {
//        return new NewTopic(Topics.EVENT_LOG, eventTopicDefinition.numPartitions, eventTopicDefinition.replicationFactor)
//                .configs(Map.of(RETENTION_MS_CONFIG, "-1", RETENTION_BYTES_CONFIG, "-1"));
//    }

    public static String stateStoreTopic(String storeName){
        return storeName+"_changeLog";
    }

    @Bean
    public NewTopic changeLogTopic() {
        return new NewTopic(stateStoreTopic(StateStores.WONDER_SEEKER_STATE_STORE), changeLogTopicDefinition.numPartitions, changeLogTopicDefinition.replicationFactor)
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
