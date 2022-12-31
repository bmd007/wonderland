package wonderland.game.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static wonderland.game.engine.config.RabbitMqConfig.RABBIT_GAME_MESSAGES_EXCHANGE;

@RestController
public class ChatResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatResource.class);
    public static final String APP_ID = "wonderland.message-publisher";

    private final AmqpTemplate rabbitTemplate;
    private final KafkaTemplate<String, MessageSentEvent> kafkaTemplate;

    public ChatResource(AmqpTemplate rabbitTemplate, KafkaTemplate<String, MessageSentEvent> kafkaTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    private static MessageProperties basicMessageProperties(SendMessageRequestBody requestBody) {
        var messageProperties = new MessageProperties();
        messageProperties.setHeader("type", requestBody.type());
        messageProperties.setMessageId(UUID.randomUUID().toString());
        messageProperties.setAppId(APP_ID);
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setCorrelationId(UUID.randomUUID().toString());
        messageProperties.setReceivedRoutingKey(requestBody.receiver());
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        messageProperties.setReceivedDeliveryMode(MessageDeliveryMode.PERSISTENT);
        messageProperties.setHeader("version", "1");
        return messageProperties;
    }

    @PostMapping("/v1/game/messages")
    public void sendGamingMessage(@RequestBody SendMessageRequestBody requestBody) {
        MessageProperties messageProperties = basicMessageProperties(requestBody);
        messageProperties.setReceivedExchange(RABBIT_GAME_MESSAGES_EXCHANGE);
        var message = new Message(requestBody.content().getBytes(StandardCharsets.UTF_8), messageProperties);
        rabbitTemplate.send(RABBIT_GAME_MESSAGES_EXCHANGE, requestBody.receiver(), message);
        LOGGER.info("game message {} sent to {} from {}", requestBody.content(), requestBody.receiver(), requestBody.sender());
    }
}
