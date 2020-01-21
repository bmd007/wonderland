package wonderland.communication.graph.domain;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "message_history", type = "message")
//@JsonDeserialize(builder = Message.Builder.class)
public class Message {

    private String id;
    private String sender;
    private String receiver;
    private String text;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime sentTime;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("sender", sender)
                .add("receiver", receiver)
                .add("text", text)
                .add("sentTime", sentTime)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equal(id, message.id) &&
                Objects.equal(sender, message.sender) &&
                Objects.equal(receiver, message.receiver) &&
                Objects.equal(text, message.text) &&
                Objects.equal(sentTime, message.sentTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, sender, receiver, text, sentTime);
    }

    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getSentTime() {
        return sentTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSentTime(LocalDateTime sentTime) {
        this.sentTime = sentTime;
    }

    public Message(String sender, String receiver, String text, LocalDateTime sentTime) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.sentTime = sentTime;
    }

    @JsonCreator
    public Message(@JsonProperty("id") String id, @JsonProperty("sender") String sender, @JsonProperty("receiver") String receiver, @JsonProperty("text") String text, @JsonProperty("sentTime") LocalDateTime sentTime) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.sentTime = sentTime;
    }
}
