package ir.tiroon.kafkatoSSE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

@Controller
public class UiResources {

    @Autowired
    private Flux<ReceiverRecord<String, String>> fluxReceiver;

    @RequestMapping("/")
    public String index(Model model){
        // loads 1 and display 1, stream data, data driven mode.
        IReactiveDataDriverContextVariable reactiveDataDrivenMode =
                new ReactiveDataDriverContextVariable(fluxReceiver.map(KafkaToSseApplication::deserializeFromString)
                        .switchIfEmpty(Flux.error(new NullPointerException())), 1);

        model.addAttribute("events", reactiveDataDrivenMode);

        return "index";
    }
}
