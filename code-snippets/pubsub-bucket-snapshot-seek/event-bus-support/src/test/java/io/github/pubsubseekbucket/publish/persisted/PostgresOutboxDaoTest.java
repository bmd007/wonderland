package io.github.pubsubseekbucket.publish.persisted;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import io.github.pubsubseekbucket.subscribe.integrationtest.Application;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {Application.class, PersistedPublisherConfiguration.class},
        properties = {"MY_POD_NAME=TEST_POD"})
public class PostgresOutboxDaoTest extends OutboxDaoTest {
    @BeforeEach
    @AfterEach
    public void cleanUpDatabase() {
        jdbcTemplate.execute("delete from outbox");
    }

    // Sadly, H2 (which is used to emulate Oracle) does not support random order.
    // Hence, this test is Postgres-only
    @Test
    void readLimitedNumberOfOldEventsFromOutboxInRandomOrder() throws InterruptedException {
        // Given
        var oldEvent1 = new OutboxEvent("TS_ID_20230910", 1, "topic1", createDummyMessage(1));
        runInTransaction(status -> target.insertInOutbox(oldEvent1));
        var oldEvent2 = new OutboxEvent("TS_ID_20230911", 1, "topic1", createDummyMessage(1));
        runInTransaction(status -> target.insertInOutbox(oldEvent2));
        var oldEvent3 = new OutboxEvent("TS_ID_20230912", 1, "topic1", createDummyMessage(1));
        runInTransaction(status -> target.insertInOutbox(oldEvent3));

        Thread.sleep(1000);

        var newEvent = new OutboxEvent("TS_ID_20230913", 1, "topic1", createDummyMessage(1));
        runInTransaction(status -> target.insertInOutbox(newEvent));

        // When
        Set<OutboxEvent> oldEvents = new HashSet<>();
        target.readOldInOutbox(Duration.of(500, ChronoUnit.MILLIS), oldEvents::add, true, 2L);

        // Then
        assertEquals(2, oldEvents.size());
        assertTrue(Set.of(oldEvent1, oldEvent2, oldEvent3).containsAll(oldEvents));
    }

}
