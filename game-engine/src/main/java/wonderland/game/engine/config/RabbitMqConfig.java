package wonderland.game.engine.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String RABBIT_MESSAGES_EXCHANGE = "messages";
    public static final String RABBIT_WEBRTC_MESSAGES_EXCHANGE = "messages/webrtc";
    public static final String RABBIT_GAME_MESSAGES_EXCHANGE = "messages/game";

    @Bean
    public DirectExchange messagesExchange() {
        return new DirectExchange(RABBIT_MESSAGES_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange webRtcMessagesExchange() {
        return new DirectExchange(RABBIT_WEBRTC_MESSAGES_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange gameMessagesExchange() {
        return new DirectExchange(RABBIT_GAME_MESSAGES_EXCHANGE, true, false);
    }
}
