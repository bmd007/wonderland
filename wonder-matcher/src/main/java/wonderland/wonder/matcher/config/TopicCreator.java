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
import static org.apache.kafka.common.config.TopicConfig.RETENTION_BYTES_CONFIG;
import static org.apache.kafka.common.config.TopicConfig.RETENTION_MS_CONFIG;

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

    @Bean
    public NewTopic wonderSeekerPassiveLikeEventsTopic() {
        return new NewTopic(Topics.WONDER_SEEKER_PASSIVE_LIKE_EVENTS, eventTopicDefinition.numPartitions, eventTopicDefinition.replicationFactor)
                .configs(Map.of(RETENTION_MS_CONFIG, "-1", RETENTION_BYTES_CONFIG, "-1"));
    }
    @Bean
    public NewTopic wonderMatcherWonderSeekerMatchEventsRepartition() {
        return new NewTopic("wonder-matcher-wonder-seeker-match-events-repartition", eventTopicDefinition.numPartitions, eventTopicDefinition.replicationFactor)
                .configs(Map.of(RETENTION_MS_CONFIG, "-1", RETENTION_BYTES_CONFIG, "-1"));
    }

//    @Bean
//    public NewTopic wonderSeekerMatchEventsTopic() {
//        return new NewTopic(Topics.WONDER_SEEKER_MATCH_EVENTS, eventTopicDefinition.numPartitions, eventTopicDefinition.replicationFactor)
//                .configs(Map.of(RETENTION_MS_CONFIG, "-1", RETENTION_BYTES_CONFIG, "-1"));
//    }

    public static String stateStoreTopicName(String storeName, String applicationName) {
        return String.format("%s-%s-changelog", applicationName, storeName);
    }

    @Bean
    public NewTopic wonderSeekerSeekChangeLogTopic() {
        return new NewTopic(stateStoreTopicName(StateStores.WONDER_SEEKER_IN_MEMORY_STATE_STORE, applicationName),
                changeLogTopicDefinition.numPartitions, changeLogTopicDefinition.replicationFactor)
                .configs(Map.of(CLEANUP_POLICY_CONFIG, CLEANUP_POLICY_COMPACT));
    }

    @Bean
    public NewTopic wonderSeekerLikeChangeLogTopic() {
        return new NewTopic(stateStoreTopicName(StateStores.WONDER_SEEKER_LIKE_HISTORY_STATE_STORE, applicationName),
                changeLogTopicDefinition.numPartitions, changeLogTopicDefinition.replicationFactor)
                .configs(Map.of(CLEANUP_POLICY_CONFIG, CLEANUP_POLICY_COMPACT));
    }
    @Bean
    public NewTopic wonderSeekerLikedByChangeLogTopic() {
        return new NewTopic(stateStoreTopicName(StateStores.WONDER_SEEKER_LIKED_BY_HISTORY_STATE_STORE, applicationName),
                changeLogTopicDefinition.numPartitions, changeLogTopicDefinition.replicationFactor)
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
