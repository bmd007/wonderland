package io.github.pubsubseekbucket.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Make sure Subscription deleted before GCP resources are closed down.
 * (Just using @PreDestroy doesn't work)
 */
@Slf4j
public class DynamicSubscriptionDeleter implements SmartLifecycle {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final SubscriptionAdminUtil subscriptionAdminUtil;

    public DynamicSubscriptionDeleter(SubscriptionAdminUtil subscriptionAdminUtil) {
        this.subscriptionAdminUtil = subscriptionAdminUtil;
        log.info("DynamicSubscriptionDeleter activated");
    }

    @Override
    public void start() {
        running.set(true);
    }

    @Override
    public void stop() {
        subscriptionAdminUtil.deleteDynamicSubscriptions();
        running.set(false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}
