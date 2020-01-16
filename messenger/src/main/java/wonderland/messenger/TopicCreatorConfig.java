package wonderland.messenger;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.regex.Pattern;

import static org.apache.kafka.common.config.TopicConfig.RETENTION_BYTES_CONFIG;
import static org.apache.kafka.common.config.TopicConfig.RETENTION_MS_CONFIG;

@Configuration
public class TopicCreatorConfig {

/**application.properties
    topic-defs:
        messageEventTopicDefinition: '8:3'
        attribute-id-mapping-updates: '8:3'
**/

    public static String MESSAGE_EVENT_TOPIC = "message_events";

//    private PartitionDef attributeIdMappingTopicDef;
    private PartitionDef messageEventTopicDefinition;

    public TopicCreatorConfig(
            @Value("${topic-defs.messageEvents}") String messageEventTopicDefinition
//            @Value("${topic-defs.attribute-id-mapping-updates}") String atrIdMappingsUpdatesTopicPartitions
    ) {
//        this.attributeIdMappingTopicDef = PartitionDef.parse(atrIdMappingsUpdatesTopicPartitions);
        this.messageEventTopicDefinition = PartitionDef.parse(messageEventTopicDefinition);
    }

    @Bean
    public NewTopic messageEventTopic() {
        return new NewTopic(MESSAGE_EVENT_TOPIC, messageEventTopicDefinition.numPartitions, messageEventTopicDefinition.replicationFactor);
    }

//    @Bean
//    public NewTopic atrIdMappingsUpdatesTopic() {
//        return new NewTopic(AttributeIdMappingService.ATTRIBUTE_ID_MAPPING_EVENTS_TOPIC,
//                attributeIdMappingTopicDef.numPartitions, attributeIdMappingTopicDef.replicationFactor)
//                .configs(Map.of(RETENTION_MS_CONFIG, "-1", RETENTION_BYTES_CONFIG, "-1"));
//    }

    private static class PartitionDef {

        private final static Pattern PATTERN = Pattern.compile("(\\d+):(\\d+)");

        private int numPartitions;
        private short replicationFactor;

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