package wonderland.message.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static wonderland.message.publisher.config.RabbitMqConfig.RABBIT_WEBRTC_MESSAGES_EXCHANGE;

@RestController
public class VideoChatSignalingResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoChatSignalingResource.class);
    public static final String APP_ID = "wonderland.message-publisher";

    private final AmqpTemplate rabbitTemplate;

    public VideoChatSignalingResource(AmqpTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    private static MessageProperties beaseMessageProperties(SendMessageRequestBody requestBody) {
        var messageProperties = new MessageProperties();
        messageProperties.setMessageId(UUID.randomUUID().toString());
        messageProperties.setAppId(APP_ID);
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setCorrelationId(UUID.randomUUID().toString());
        messageProperties.setReceivedExchange(RABBIT_WEBRTC_MESSAGES_EXCHANGE);
        messageProperties.setReceivedRoutingKey(requestBody.receiver());
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        messageProperties.setReceivedDeliveryMode(MessageDeliveryMode.PERSISTENT);
        messageProperties.setHeader("version", "1");
        return messageProperties;
    }

    @PostMapping("/v1/video/chat/offer")
    public String sendOffer(@RequestBody SendMessageRequestBody requestBody) {
        try {
            MessageProperties messageProperties = beaseMessageProperties(requestBody);
            messageProperties.setHeader("type", "WebRtcOffer");
            var message = new Message(requestBody.content().getBytes(StandardCharsets.UTF_8), messageProperties);
            rabbitTemplate.send(RABBIT_WEBRTC_MESSAGES_EXCHANGE, requestBody.receiver(), message);
            LOGGER.info("offer {} sent to {} from {}", requestBody.content(), requestBody.receiver(), requestBody.sender());
            return requestBody.content() + " sent to " + requestBody.receiver() + " at " + LocalDateTime.now();
        } catch (AmqpException e) {
            return e.getMessage();
        }
    }

    @PostMapping("/v1/video/chat/answer")
    public String sendAnswer(@RequestBody SendMessageRequestBody requestBody) {
        try {
            MessageProperties messageProperties = beaseMessageProperties(requestBody);
            messageProperties.setHeader("type", "WebRtcAnswer");
            var message = new Message(requestBody.content().getBytes(StandardCharsets.UTF_8), messageProperties);
            rabbitTemplate.send(RABBIT_WEBRTC_MESSAGES_EXCHANGE, requestBody.receiver(), message);
            LOGGER.info("answer {} sent to {} from {}", requestBody.content(), requestBody.receiver(), requestBody.sender());
            return requestBody.content() + " sent to " + requestBody.receiver() + " at " + LocalDateTime.now();
        } catch (AmqpException e) {
            return e.getMessage();
        }
    }

    @PostMapping("/v1/video/chat/candidate")
    public String sendCandidate(@RequestBody SendMessageRequestBody requestBody) {
        try {
            MessageProperties messageProperties = beaseMessageProperties(requestBody);
            messageProperties.setHeader("type", "WebRtcCandidate");
            var message = new Message(requestBody.content().getBytes(StandardCharsets.UTF_8), messageProperties);
            rabbitTemplate.send(RABBIT_WEBRTC_MESSAGES_EXCHANGE, requestBody.receiver(), message);
            LOGGER.info("candidate {} sent to {} from {}", requestBody.content(), requestBody.receiver(), requestBody.sender());
            return requestBody.content() + " sent to " + requestBody.receiver() + " at " + LocalDateTime.now();
        } catch (AmqpException e) {
            return e.getMessage();
        }
    }
}
