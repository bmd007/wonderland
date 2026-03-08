package io.github.pubsubseekbucket.util;

import java.time.Duration;

@SuppressWarnings("PMD.LinguisticNaming")
public class SubscriptionConfiguration {
    private String deadLetterTopic;
    private Integer maxDeliveryAttempts;
    private Duration minBackoff;
    private Duration maxBackoff;
    private Duration expiry;
    private Duration ackDeadline;

    public String getDeadLetterTopic() {
        return deadLetterTopic;
    }

    public SubscriptionConfiguration setDeadLetterTopic(String deadLetterTopic) {
        this.deadLetterTopic = deadLetterTopic;
        return this;
    }

    public Integer getMaxDeliveryAttempts() {
        return maxDeliveryAttempts;
    }

    public SubscriptionConfiguration setMaxDeliveryAttempts(Integer maxDeliveryAttempts) {
        this.maxDeliveryAttempts = maxDeliveryAttempts;
        return this;
    }

    public Duration getMinBackoff() {
        return minBackoff;
    }

    public SubscriptionConfiguration setMinBackoff(Duration minBackoff) {
        this.minBackoff = minBackoff;
        return this;
    }

    public Duration getMaxBackoff() {
        return maxBackoff;
    }

    public SubscriptionConfiguration setMaxBackoff(Duration maxBackoff) {
        this.maxBackoff = maxBackoff;
        return this;
    }

    public Duration getExpiry() {
        return expiry;
    }

    public SubscriptionConfiguration setExpiry(Duration expiry) {
        this.expiry = expiry;
        return this;
    }

    public Duration getAckDeadline() {
        return ackDeadline;
    }

    public SubscriptionConfiguration setAckDeadline(Duration ackDeadline) {
        this.ackDeadline = ackDeadline;
        return this;
    }
}
