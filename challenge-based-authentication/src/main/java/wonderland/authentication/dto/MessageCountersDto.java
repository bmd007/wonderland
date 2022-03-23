package wonderland.authentication.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.List;

public class MessageCountersDto {
    private List<MessageCounterDto> messageCounters;

    @JsonCreator
    public MessageCountersDto(@JsonProperty("counters") List<MessageCounterDto> messageCounters) {
        this.messageCounters = messageCounters;
    }

    public List<MessageCounterDto> getMessageCounters() {
        return messageCounters;
    }

    public void setMessageCounters(List<MessageCounterDto> messageCounters) {
        this.messageCounters = messageCounters;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("messageCounters", messageCounters)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageCountersDto that = (MessageCountersDto) o;
        return Objects.equal(messageCounters, that.messageCounters);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(messageCounters);
    }
}
