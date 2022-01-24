package wonderland.message.counter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MessageCounterApplication {
    //the whole project could be done with spring.cloud.streams mixed with spring.functions. More config less code. maybe another day another life :)
    public static void main(String[] args) {
        SpringApplication.run(MessageCounterApplication.class, args);
    }
}
