package wonderland.message.publisher;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import wonderland.message.publisher.config.Topics;

import java.time.Instant;

@RestController
public class MessageResource {

    private final AmqpTemplate rabbitTemplate;
    private final AmqpAdmin rabbitAdmin;
    private final DirectExchange messagesExchange;
    private final KafkaTemplate<String, MessageSentEvent> kafkaTemplate;

    public MessageResource(AmqpTemplate rabbitTemplate, AmqpAdmin rabbitAdmin, DirectExchange messagesExchange, KafkaTemplate<String, MessageSentEvent> kafkaTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = rabbitAdmin;
        this.messagesExchange = messagesExchange;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/send/message/{from}/{to}")
    public String sendMessage(@PathVariable String from, @PathVariable String to, @RequestBody String body) {
        try {
            rabbitTemplate.convertAndSend(messagesExchange.getName(), to, body);
            var sendTime = Instant.now();
            var messageSentEvent = MessageSentEvent.builder()
                    .body(body)
                    .from(from)
                    .to(to)
                    .time(sendTime)
                    .build();
            var producerRecord = new ProducerRecord<String, MessageSentEvent>(Topics.MESSAGES_EVENTS_TOPIC, from, messageSentEvent);
            kafkaTemplate.send(producerRecord);
            return body + " sent to " + to + " at " + sendTime;
        } catch (AmqpException e) {
            return e.getMessage();
        }
    }

    @PostMapping("/create/queue/for/{email}")
    public String createQueue(@PathVariable String email) {
        var queue = new Queue(email, true, false, false);
        var binding = new Binding(email, Binding.DestinationType.QUEUE, messagesExchange.getName(), email, null);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
        return queue.getName() + ":" + queue.getActualName() + " is created and bind to " + messagesExchange.getName() + " exchange";
    }

    @PostMapping("/prepare/then/send/message/{from}/{to}")
    public String prepareThenSendMessage(@PathVariable String from, @PathVariable String to, @RequestBody String body) {
        createQueue(to);
        return sendMessage(from, to, body);
    }
}
