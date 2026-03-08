package io.github.pubsubseekbucket.publish.persisted;

@FunctionalInterface
public interface PublishTrigger extends Runnable {

    /**
     * Trigger immediate publishing of persisted events. Should only be called after all transactional operations in the same scope are committed.
     */
    void publish();

    @Override
    default void run() {
        publish();
    }
}
