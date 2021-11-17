package wonderland.message.publisher.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Bean
    DirectExchange messagesExchange() {
        return new DirectExchange("messages");
    }
}
