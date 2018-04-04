package ir.tiroon.microservices.bean

import ir.tiroon.microservices.model.PersonInterest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.client.RestTemplate

import javax.faces.view.ViewScoped
import javax.inject.Named

//@Component("dummy")
//@Scope("Session")
@Named("PersonInterestsBean")
@ViewScoped
class PersonInterestsBean {

    @Autowired
    RestTemplate restTemplate


    ArrayList<String> getTheInterests() {
        ArrayList<String> ints = new ArrayList<>()

        Set<String> interests = restTemplate.getForObject(
                "http://localhost:8083/show/interests/09398240640", PersonInterest.class).interests

        ints.add(interests)

        ints
    }
}