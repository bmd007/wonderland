package wonderland.wonder.matcher;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import wonderland.wonder.matcher.config.StateStores;
import wonderland.wonder.matcher.config.Topics;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {
        Topics.WONDER_SEEK_UPDATES_TOPIC,
        StateStores.WONDER_SEEKER_STATE_STORE + "_changeLog",
        StateStores.WONDER_SEEKER_GLOBAL_STATE_STORE + "_changeLog",
})
public class ApplicationTests {

    @Test
    public void contextLoads() {
    }
}
