package wonderland.api.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(Boolean.TRUE);
    }

}
