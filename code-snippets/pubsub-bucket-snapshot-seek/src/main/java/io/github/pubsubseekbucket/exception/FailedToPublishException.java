package io.github.pubsubseekbucket.exception;
public class FailedToPublishException extends RuntimeException {
    static final long serialVersionUID = 42L;

    public FailedToPublishException(Object messageToPublish, Throwable e) {
        super("Failed to publish message: " + messageToPublish, e);
    }
}
