package io.github.pubsubseekbucket.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionAdminUtilTest {

    @InjectMocks
    SubscriptionAdminUtil target;

    @Test
    void deleteDynamicSubscriptions() {
        // Should not throw Exception when called without dynamic subscriptions
        target.deleteDynamicSubscriptions();
    }
}
