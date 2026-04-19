package io.github.bmd007.wonderland.hesab_ketab;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.EnableWireMock;

import static org.assertj.core.api.Assertions.fail;

@EnableWireMock
@ActiveProfiles({"local-test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTest {

    @RegisterExtension
    static ContractTestExtension contractTestExtension = new ContractTestExtension();

    @Autowired
    JdbcClient jdbcClient;

    @AfterEach
    void afterEach() {
        if (!WireMock.findUnmatchedRequests().isEmpty()) {
            fail("Unmatched requests: " + WireMock.findUnmatchedRequests());
        }
        WireMock.reset();
        jdbcClient.sql("DELETE FROM event_consumer_offsets").update();
        jdbcClient.sql("DELETE FROM domain_events").update();
        jdbcClient.sql("DELETE FROM scheduled_tasks").update();
        jdbcClient.sql("DELETE FROM accounts").update();
    }

    @Test
    void contextLoads() {
        jdbcClient.sql("select 1")
            .query()
            .optionalValue()
            .ifPresentOrElse(o -> {
            }, () -> fail("Failed to execute query, datasource might not be configured properly"));
    }
}
