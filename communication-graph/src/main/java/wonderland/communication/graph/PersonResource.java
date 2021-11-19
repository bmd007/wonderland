package wonderland.communication.graph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wonderland.communication.graph.repository.PersonInfluenceRankDto;
import wonderland.communication.graph.repository.PersonRepository;

import java.util.List;

@RestController
public class PersonResource {

    private final PersonRepository personRepository;

    public PersonResource(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GetMapping("/most/influential/person")
    public List<String> getMostInfluentialPerson() {
        return personRepository.getInfluenceRank();
    }
}
