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

@RestController
public class VideoChatSignalingResource {

    public static final String APP_ID = "wonderland.message-publisher";
    private final AmqpTemplate rabbitTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoChatSignalingResource.class);
    private final String RABBIT_MQ_MESSAGES_EXCHANGE = "messages";

    public VideoChatSignalingResource(AmqpTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/v1/video/chat/offer")
    public String sendOffer(@RequestBody SendMessageRequestBody requestBody) {
        try {
            var messageProperties = new MessageProperties();
            messageProperties.setMessageId(UUID.randomUUID().toString());
            messageProperties.setAppId(APP_ID);
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setCorrelationId(UUID.randomUUID().toString());
            messageProperties.setReceivedExchange(RABBIT_MQ_MESSAGES_EXCHANGE);
            messageProperties.setReceivedRoutingKey(requestBody.receiver());
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            messageProperties.setReceivedDeliveryMode(MessageDeliveryMode.PERSISTENT);
            messageProperties.setHeader("type", "WebRtcOffer");
            messageProperties.setHeader("version", "1");
            var message = new Message(requestBody.content().getBytes(StandardCharsets.UTF_8), messageProperties);
            rabbitTemplate.send(RABBIT_MQ_MESSAGES_EXCHANGE, requestBody.receiver(), message);
            LOGGER.info("offer {} sent to {} from {}", requestBody.content(), requestBody.receiver(), requestBody.sender());
            return requestBody.content() + " sent to " + requestBody.receiver() + " at " + LocalDateTime.now();
        } catch (AmqpException e) {
            return e.getMessage();
        }
    }

    @PostMapping("/v1/video/chat/answer")
    public String sendAnswer(@RequestBody SendMessageRequestBody requestBody) {
        try {
            var messageProperties = new MessageProperties();
            messageProperties.setMessageId(UUID.randomUUID().toString());
            messageProperties.setAppId(APP_ID);
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setCorrelationId(UUID.randomUUID().toString());
            messageProperties.setReceivedExchange(RABBIT_MQ_MESSAGES_EXCHANGE);
            messageProperties.setReceivedRoutingKey(requestBody.receiver());
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            messageProperties.setReceivedDeliveryMode(MessageDeliveryMode.PERSISTENT);
            messageProperties.setHeader("type", "WebRtcAnswer");
            messageProperties.setHeader("version", "1");
            var message = new Message(requestBody.content().getBytes(StandardCharsets.UTF_8), messageProperties);
            rabbitTemplate.send(RABBIT_MQ_MESSAGES_EXCHANGE, requestBody.receiver(), message);
            LOGGER.info("answer {} sent to {} from {}", requestBody.content(), requestBody.receiver(), requestBody.sender());
            return requestBody.content() + " sent to " + requestBody.receiver() + " at " + LocalDateTime.now();
        } catch (AmqpException e) {
            return e.getMessage();
        }
    }

    @PostMapping("/v1/video/chat/candidate")
    public String sendCandidate(@RequestBody SendMessageRequestBody requestBody) {
        try {
            var messageProperties = new MessageProperties();
            messageProperties.setMessageId(UUID.randomUUID().toString());
            messageProperties.setAppId(APP_ID);
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setCorrelationId(UUID.randomUUID().toString());
            messageProperties.setReceivedExchange(RABBIT_MQ_MESSAGES_EXCHANGE);
            messageProperties.setReceivedRoutingKey(requestBody.receiver());
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            messageProperties.setReceivedDeliveryMode(MessageDeliveryMode.PERSISTENT);
            messageProperties.setHeader("type", "WebRtcCandidate");
            messageProperties.setHeader("version", "1");
            var message = new Message(requestBody.content().getBytes(StandardCharsets.UTF_8), messageProperties);
            rabbitTemplate.send(RABBIT_MQ_MESSAGES_EXCHANGE, requestBody.receiver(), message);
            LOGGER.info("candidate {} sent to {} from {}", requestBody.content(), requestBody.receiver(), requestBody.sender());
            return requestBody.content() + " sent to " + requestBody.receiver() + " at " + LocalDateTime.now();
        } catch (AmqpException e) {
            return e.getMessage();
        }
    }

}
