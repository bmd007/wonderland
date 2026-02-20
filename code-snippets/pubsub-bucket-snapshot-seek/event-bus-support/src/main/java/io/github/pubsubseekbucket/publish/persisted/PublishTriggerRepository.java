package io.github.pubsubseekbucket.publish.persisted;

import java.util.ArrayList;
import java.util.List;

public class PublishTriggerRepository {
    private final ThreadLocal<List<PublishTrigger>> triggers = ThreadLocal.withInitial(ArrayList::new);

    public void add(PublishTrigger trigger) {
        triggers.get().add(trigger);
    }

    public void publish() {
        var publishTriggers = triggers.get();
        try {
            publishTriggers.forEach(PublishTrigger::publish);
        } finally {
            publishTriggers.clear();
        }
    }

    public void clear() {
        triggers.get().clear();
    }
}
