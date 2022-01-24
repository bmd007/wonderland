package wonderland.message.counter.resource;

import org.springframework.http.HttpStatus;
import wonderland.message.counter.dto.MessageCounterDto;
import wonderland.message.counter.dto.MessageCountersDto;
import wonderland.message.counter.event.internal.CounterIncreasedEvent;
import wonderland.message.counter.event.internal.CounterRestartedEvent;
import wonderland.message.counter.event.internal.EventLogger;
import wonderland.message.counter.exception.NotFoundException;
import wonderland.message.counter.service.MessageCounterViewService;
import wonderland.message.counter.service.ViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/counter/message")
public class MessageCounterResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageCounterResource.class);

    private MessageCounterViewService messageCounterViewService;
    private EventLogger eventLogger;

    public MessageCounterResource(EventLogger eventLogger, MessageCounterViewService messageCounterViewService) {
        this.eventLogger = eventLogger;
        this.messageCounterViewService = messageCounterViewService;
    }

    //creating events here directly is not the best idea.
    //best practise is to have commands, command producers and command handlers
    @PostMapping("/{sender}/restart")
    @ResponseStatus(HttpStatus.CREATED)
    public void createACounter(@PathVariable String sender) {
        var event = new CounterRestartedEvent(sender);
        eventLogger.log(event);
    }

    //isHighLevelQuery query param is related to inter instance communication and it should be true in normal operations or not defined
    @GetMapping("/sent/from")
    public Mono<MessageCountersDto> getCounters(@RequestParam(required = false, value = ViewService.HIGH_LEVEL_QUERY_PARAM_NAME, defaultValue = "true") boolean isHighLevelQuery) {
        return messageCounterViewService.getAll(isHighLevelQuery);
    }

    @GetMapping("/sent/all")
    public Mono<Integer> getCountersSum(@RequestParam(required = false, value = ViewService.HIGH_LEVEL_QUERY_PARAM_NAME, defaultValue = "true") boolean isHighLevelQuery) {
        return messageCounterViewService.getAll(isHighLevelQuery)
                .flatMapIterable(messageCountersDto -> messageCountersDto.getMessageCounters())
                .map(messageCounterDto -> messageCounterDto.getNumberOfSentMessages())
                .reduce((integer, integer2) -> integer + integer2);
    }

    @GetMapping("/sent/from/{sender}")
    public Mono<MessageCounterDto> getCounterByName(@PathVariable("sender") String sender) {
        return messageCounterViewService.getById(sender)
                .switchIfEmpty(Mono.error(new NotFoundException(String.format("%s not found (%s doesn't exist). Maybe has not sent any messages yet", "Sender", sender))));
    }
}
