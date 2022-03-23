package wonderland.authentication.service;

import wonderland.authentication.config.Stores;
import wonderland.authentication.dto.MessageCounterDto;
import wonderland.authentication.dto.MessageCountersDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class MessageCounterViewService extends ViewService<MessageCountersDto, MessageCounterDto, Integer> {

    final static Function<MessageCountersDto, List<MessageCounterDto>> LIST_EXTRACTOR = MessageCountersDto::getMessageCounters;
    final static Function<List<MessageCounterDto>, MessageCountersDto> LIST_WRAPPER = MessageCountersDto::new;
    final static BiFunction<String, Integer, MessageCounterDto> DTO_MAPPER = MessageCounterDto::new;

    public MessageCounterViewService(StreamsBuilderFactoryBean streams,
                                     @Value("${kafka.streams.server.config.app-ip}") String ip,
                                     @Value("${kafka.streams.server.config.app-port}") int port,
                                     ViewResourcesClient commonClient) {
        super(ip, port, streams, Stores.MESSAGE_COUNTER_STATE,
                MessageCountersDto.class, MessageCounterDto.class, DTO_MAPPER, LIST_EXTRACTOR, LIST_WRAPPER, "sent/from", commonClient);
    }
}
