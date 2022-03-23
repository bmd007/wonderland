package wonderland.authentication.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class to automatically create the topics with the configured partitions and replication factor.
 */
@Configuration
@Profile("test")
public class TopicCreationConfig {

    @Bean
    public NewTopic eventsTopic() {
        return new NewTopic(Topics.MESSAGE_EVENT_TOPIC, 1, (short) 1);
    }

}
