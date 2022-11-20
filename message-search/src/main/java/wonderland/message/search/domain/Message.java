package wonderland.message.search.domain;


import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Value
@Builder
@Document(indexName = "message_history")
public class Message {
    @Id
    private String id;
    @Field(type = FieldType.Text)
    private String sender;
    @Field(type = FieldType.Text)
    private String receiver;
    @Field(type = FieldType.Text)
    private String text;
    @Field(type = FieldType.Date, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime sentAt;

    public static Message define(String sender, String receiver, String text, Instant time) {
        var id = UUID.randomUUID().toString();
        return new Message(id, sender, receiver, text, time.atZone(ZoneId.systemDefault()));
    }
}
//todo use @GeoPoint Point location, or GeoJsonPoint location,
