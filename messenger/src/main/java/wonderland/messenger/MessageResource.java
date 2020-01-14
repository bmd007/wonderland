package wonderland.messenger;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MessageResource {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin rabbitAdmin;

    @Autowired
    private DirectExchange messagesExchange;

    @PostMapping("/send/message/{from}/{to}")
    public String sendMessage(@PathVariable String from, @PathVariable String to, @RequestBody String body){
        rabbitTemplate.convertAndSend(messagesExchange.getName(), to, body);
        return body+" sent to "+to;
    }

    @PostMapping("/create/queue/for/{email}")
    public String createQueue(@PathVariable String email) {
        var queue = new Queue(email, true, false, false);
        var binding = new Binding(email, Binding.DestinationType.QUEUE, messagesExchange.getName(), email, null);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
        return queue.getName()+":"+queue.getActualName()+" is created and bind to "+messagesExchange.getName()+" exchange";
    }
}
