package io.github.pubsubseekbucket.subscribe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.pubsubseekbucket.publish.Publisher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PublisherTest {
    public static final String TOPIC_NAME = "topic";
    @Mock
    private PubSubTemplate pubSubTemplate;

    private Publisher target;

    ObjectMapper objectMapper = new ObjectMapper();

    private final CompletableFuture<String> completableFuture = new CompletableFuture<>();

    @BeforeEach
    public void beforeEach() {
        target = new Publisher(TOPIC_NAME, pubSubTemplate, objectMapper);

        when(pubSubTemplate.publish(eq(TOPIC_NAME), any(PubsubMessage.class))).thenReturn(completableFuture);
    }

    @Test
    void publish() throws JsonProcessingException {
        var payload = new Payload(17, "foo");

        // When
        target.publish(payload);

        //then
        verify(pubSubTemplate, times(1)).publish(TOPIC_NAME, PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(objectMapper.writeValueAsString(payload)))
                .build());
    }

    @Test
    void publishIsAcked() throws ExecutionException, InterruptedException {
        var payload = new Payload(17, "foo");

        var result = target.publish(payload);

        // When
        completableFuture.complete("Foo");

        //then
        assertTrue(result.isDone());
        assertEquals("Foo", result.get());
    }

    @Test
    void publishIsNacked() {
        var payload = new Payload(17, "foo");

        var result = target.publish(payload);

        // When
        completableFuture.completeExceptionally(new RuntimeException("Bar"));

        //then
        assertTrue(result.isDone());
        assertTrue(result.isCompletedExceptionally());
        assertThrows(ExecutionException.class, result::get);
    }

    @Value
    @Builder(toBuilder = true)
    @With
    @Jacksonized
    private static class Payload {
        int number;
        String name;
    }
}

