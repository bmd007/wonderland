package io.github.pubsubseekbucket.publish.persisted;

public class FailedToPersistException extends RuntimeException {
    public FailedToPersistException(String msg) {
        super(msg);
    }

    public FailedToPersistException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
