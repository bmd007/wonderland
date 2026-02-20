package io.github.pubsubseekbucket.publish.persisted;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PublishTriggerRepositoryTest {

    PublishTriggerRepository target = new PublishTriggerRepository();

    @Test
    void registerAndPublish() {
        // Given
        target.clear();
        AtomicBoolean b1 = new AtomicBoolean(false);
        AtomicBoolean b2 = new AtomicBoolean(false);
        AtomicBoolean b3 = new AtomicBoolean(false);

        // When
        target.add(() -> b1.set(true));
        target.add(() -> b2.set(true));

        // Then
        assertFalse(b1.get());
        assertFalse(b2.get());
        assertFalse(b3.get());

        // When
        target.publish();

        // Then
        assertTrue(b1.get());
        assertTrue(b2.get());
        assertFalse(b3.get());

        // Given
        b1.set(false);
        b2.set(false);

        // When
        target.add(() -> b3.set(true));
        target.publish();

        // Then
        assertFalse(b1.get());
        assertFalse(b2.get());
        assertTrue(b3.get());
    }

}
