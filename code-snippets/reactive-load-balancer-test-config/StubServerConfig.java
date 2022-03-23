

import com.xebialabs.restito.server.StubServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@ActiveProfiles("test")
public class StubServerConfig {

    @Bean
    public StubServer stubServer() {
        return new StubServer().run();
    }

}
