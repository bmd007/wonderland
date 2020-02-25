package wonderland.communication.graph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wonderland.communication.graph.domain.PersonInfluenceRankDto;
import wonderland.communication.graph.repository.CommunicationRepository;
import wonderland.communication.graph.repository.PersonRepository;

import java.util.List;

@RestController
public class PersonResource {

    @Autowired
    PersonRepository personRepository;

    @GetMapping("/most/influential/person")
    public List<PersonInfluenceRankDto> getMostInfluentialPerson(){
        return personRepository.getInfluenceRank();
    }

    @Autowired
    CommunicationRepository communicationRepository;

    @GetMapping("/count/messages")
    public Mono<Long> getNumberOfSavedMessages(){
        return Flux.fromIterable(communicationRepository.findAll()).count();
    }
}
