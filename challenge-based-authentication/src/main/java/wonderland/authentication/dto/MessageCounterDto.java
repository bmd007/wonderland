package wonderland.authentication.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

//todo rename. This name is shitty :))
public class MessageCounterDto {
    private String sender;
    private Integer numberOfSentMessages;

    @JsonCreator
    public MessageCounterDto(@JsonProperty("sender") String sender, @JsonProperty("numberOfSentMessages") Integer numberOfSentMessages) {
        this.sender = sender;
        this.numberOfSentMessages = numberOfSentMessages;
    }

    public MessageCounterDto(Integer numberOfSentMessages) {
        this.numberOfSentMessages = numberOfSentMessages;
    }

    public Integer getNumberOfSentMessages() {
        return numberOfSentMessages;
    }

    public void setNumberOfSentMessages(Integer numberOfSentMessages) {
        this.numberOfSentMessages = numberOfSentMessages;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("sender", sender)
                .add("numberOfSentMessages", numberOfSentMessages)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageCounterDto that = (MessageCounterDto) o;
        return Objects.equal(sender, that.sender) &&
                Objects.equal(numberOfSentMessages, that.numberOfSentMessages);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sender, numberOfSentMessages);
    }
}
