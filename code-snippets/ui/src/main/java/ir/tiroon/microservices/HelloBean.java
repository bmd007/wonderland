package ir.tiroon.microservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

//@Component("dummy")
//@Scope("Session")
@Named("HelloBean")
@ViewScoped
public class HelloBean {

    @Autowired
    RestTemplate restTemplate;

    String getHelloMsg() {
        //TODO ask from gateway
        return restTemplate.getForEntity("http://hello", String.class).getBody();
    }
}