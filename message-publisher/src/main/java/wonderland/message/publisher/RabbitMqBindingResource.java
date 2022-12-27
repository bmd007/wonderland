package wonderland.message.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static wonderland.message.publisher.config.RabbitMqConfig.RABBIT_GAME_MESSAGES_EXCHANGE;
import static wonderland.message.publisher.config.RabbitMqConfig.RABBIT_MESSAGES_EXCHANGE;
import static wonderland.message.publisher.config.RabbitMqConfig.RABBIT_WEBRTC_MESSAGES_EXCHANGE;

@RestController
public class RabbitMqBindingResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqBindingResource.class);

    private final AmqpAdmin rabbitAdmin;

    public RabbitMqBindingResource(AmqpAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    @PostMapping(path = "/bindings/user/{email}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String createQueue(@PathVariable String email) {
        var queue = new Queue(email, true, false, false);
        var binding = new Binding(email, Binding.DestinationType.QUEUE, RABBIT_MESSAGES_EXCHANGE, email, null);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
        LOGGER.info("queue {} created", queue);

        var webrtcQueue = new Queue("webrtc/" + email, true, false, false);
        var webrtcBinding = new Binding("webrtc/" + email, Binding.DestinationType.QUEUE, RABBIT_WEBRTC_MESSAGES_EXCHANGE, email, null);
        rabbitAdmin.declareQueue(webrtcQueue);
        rabbitAdmin.declareBinding(webrtcBinding);
        LOGGER.info("queue {} created", webrtcQueue);

        var gameQueue = new Queue("game/" + email, true, false, false);
        var gameBinding = new Binding("game/" + email, Binding.DestinationType.QUEUE, RABBIT_GAME_MESSAGES_EXCHANGE, email, null);
        rabbitAdmin.declareQueue(gameQueue);
        rabbitAdmin.declareBinding(gameBinding);
        LOGGER.info("queue {} created", gameQueue);

        return queue.getName() + ":" + queue.getActualName() + " is created and bind to " + "messages" + " exchange";
    }

}
