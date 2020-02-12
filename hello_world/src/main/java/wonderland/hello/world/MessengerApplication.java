package wonderland.hello.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;

@RestController
@SpringBootApplication
public class MessengerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MessengerApplication.class, args);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MessengerApplication.class);

    @GetMapping
    public String hello(){
        return "HELLO";
    }

    @Override
    public void run(String... args) throws Exception {
        InetAddress inetAddress = InetAddress.getLocalHost();
        System.out.println("IP Address:- " + inetAddress.getHostAddress());
        System.out.println("Host Name:- " + inetAddress.getHostName());
    }
}
