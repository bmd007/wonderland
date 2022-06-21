package wonderland.message.publisher.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Bean
    TopicExchange messagesExchange() {
        return new TopicExchange("messages");
    }
}
