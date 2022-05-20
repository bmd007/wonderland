package wonderland.wonder.matcher.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

@Configuration
@Profile("test")
public class EmbeddedKafkaConfig {

    /**
     * This must be done during construction phase, not after.
     */
    @Bean
    public KafkaStreamsAwait kafkaStreamsAwait(StreamsBuilderFactoryBean streams) {
        var await = new KafkaStreamsAwait();
        streams.setStateListener(await);
        return await;
    }
}
