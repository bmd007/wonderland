package io.github.bmd007.wonderland.hesab_ketab;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Blob;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

@Slf4j
@AutoConfigureWireMock
@ActiveProfiles({"local-test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTest {

    @RegisterExtension
    static ContractTestExtension contractTestExtension = new ContractTestExtension();

    @Value("${spring.application.name}")
    String applicationName;
    @Autowired
    JdbcClient jdbcClient;

    String instrumentBucketName = "nordnet-dummy-project-v2-instruments-dump";
    String instrumentMappingBucketName = "nordnet-dummy-project-v1-instrument-mappings-dump";

    @AfterEach
    void afterEach() {
        if (!WireMock.findUnmatchedRequests().isEmpty()) {
            fail("Unmatched requests: " + WireMock.findUnmatchedRequests());
        }
        WireMock.reset();
        jdbcClient.sql("delete from atable").update();
    }

    @Test
    void contextLoads() {
        jdbcClient.sql("select 1")
            .query()
            .optionalValue()
            .ifPresentOrElse(o -> {
            }, () -> fail("Failed to execute query, datasource might not be configured properly"));
    }

    private void drainTopic(String topic) {

    }
}
