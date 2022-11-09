package wonderland.message.publisher;

import io.micrometer.core.instrument.Metrics;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wonderland.message.publisher.config.Topics;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
public class VideoChatSignalingResource {

    private final AmqpTemplate rabbitTemplate;
    private final AmqpAdmin rabbitAdmin;
    private final KafkaTemplate<String, MessageSentEvent> kafkaTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoChatSignalingResource.class);

    public VideoChatSignalingResource(AmqpTemplate rabbitTemplate, AmqpAdmin rabbitAdmin, KafkaTemplate<String, MessageSentEvent> kafkaTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = rabbitAdmin;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/v1/video/chat/offer")
    public String sendOffer(@RequestBody SendMessageRequestBody requestBody){
        try {
            var messageProperties = new MessageProperties();
            messageProperties.setMessageId(UUID.randomUUID().toString());
            messageProperties.setAppId("wonderland.message-publisher");
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setCorrelationId(UUID.randomUUID().toString());
            messageProperties.setReceivedExchange("messages");
            messageProperties.setReceivedRoutingKey(requestBody.receiver());
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            messageProperties.setReceivedDeliveryMode(MessageDeliveryMode.PERSISTENT);
            messageProperties.setHeader("type", "WebRtcOffer");
            messageProperties.setHeader("version", "1");
            var message = new Message(requestBody.content().getBytes(StandardCharsets.UTF_8), messageProperties);
            rabbitTemplate.send("messages", requestBody.receiver(), message);
            LOGGER.info("offer {} sent to {} from {}", requestBody.content(), requestBody.receiver(), requestBody.sender());
            return requestBody.content() + " sent to " + requestBody.receiver() + " at " + LocalDateTime.now();
        } catch (AmqpException e) {
            return e.getMessage();
        }
    }

    @PostMapping("/v1/video/chat/answer")
    public String sendAnswer(@RequestBody SendMessageRequestBody requestBody){
        try {
            var messageProperties = new MessageProperties();
            messageProperties.setMessageId(UUID.randomUUID().toString());
            messageProperties.setAppId("wonderland.message-publisher");
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setCorrelationId(UUID.randomUUID().toString());
            messageProperties.setReceivedExchange("messages");
            messageProperties.setReceivedRoutingKey(requestBody.receiver());
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            messageProperties.setReceivedDeliveryMode(MessageDeliveryMode.PERSISTENT);
            messageProperties.setHeader("type", "WebRtcAnswer");
            messageProperties.setHeader("version", "1");
            var message = new Message(requestBody.content().getBytes(StandardCharsets.UTF_8), messageProperties);
            rabbitTemplate.send("messages", requestBody.receiver(), message);
            LOGGER.info("answer {} sent to {} from {}", requestBody.content(), requestBody.receiver(), requestBody.sender());
            return requestBody.content() + " sent to " + requestBody.receiver() + " at " + LocalDateTime.now();
        } catch (AmqpException e) {
            return e.getMessage();
        }
    }

}
