package io.github.pubsubseekbucket.publish.persisted;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import io.github.pubsubseekbucket.subscribe.integrationtest.Application;

import javax.sql.DataSource;

// This test uses in-memory H2 in Oracle mode
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {Application.class, PersistedPublisherConfiguration.class},
        properties = {
                "MY_POD_NAME=TEST_POD",
                "eventbus.publish.outbox.databaseManager=oracle",
                "eventbus.publish.outbox.schema=myschema",
                "eventbus.publish.outbox.permitRandomOrder=false",
                "eventbus.publish.drainFrequencyInSeconds=100000"
        })
@ActiveProfiles({"oracle"})
public class OracleOutboxDaoTest extends OutboxDaoTest {
    @Autowired
    DataSource dataSource;

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        new Flyway(Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/ora/migrations")
                .installedBy("UnitTest")
        ).migrate();
    }

    @BeforeEach
    @AfterEach
    public void cleanUpDatabase() {
        jdbcTemplate.execute("delete from myschema.outbox");
    }
}
