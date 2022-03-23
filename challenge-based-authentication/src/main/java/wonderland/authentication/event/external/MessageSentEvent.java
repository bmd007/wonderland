package wonderland.authentication.event.external;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.time.Instant;

@JsonDeserialize(builder = MessageSentEvent.Builder.class)
public class MessageSentEvent {
    private String from;
    private String to;
    private String body;
    private Instant time;

    private MessageSentEvent(Builder builder) {
        from = builder.from;
        to = builder.to;
        body = builder.body;
        time = builder.time;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder newBuilder(MessageSentEvent copy) {
        Builder builder = new Builder();
        builder.from = copy.getFrom();
        builder.to = copy.getTo();
        builder.body = copy.getBody();
        builder.time = copy.getTime();
        return builder;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("from", from)
                .add("to", to)
                .add("body", body)
                .add("time", time)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageSentEvent that = (MessageSentEvent) o;
        return Objects.equal(from, that.from) &&
                Objects.equal(to, that.to) &&
                Objects.equal(body, that.body) &&
                Objects.equal(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(from, to, body, time);
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getBody() {
        return body;
    }

    public Instant getTime() {
        return time;
    }

    public static final class Builder {
        private String from;
        private String to;
        private String body;
        private Instant time;

        private Builder() {
        }

        public Builder withFrom(String val) {
            from = val;
            return this;
        }

        public Builder withTo(String val) {
            to = val;
            return this;
        }

        public Builder withBody(String val) {
            body = val;
            return this;
        }

        public Builder withTime(Instant val) {
            time = val;
            return this;
        }

        public MessageSentEvent build() {
            return new MessageSentEvent(this);
        }
    }
}
