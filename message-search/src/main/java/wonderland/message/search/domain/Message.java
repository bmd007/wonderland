package wonderland.message.search.domain;


import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Value
@Builder
@Document(indexName = "message_history")
public class Message {
    @Id private String id;
    @Field(type = FieldType.Text) private String sender;
    @Field(type = FieldType.Text) private String receiver;
    @Field(type = FieldType.Text) private String text;
    @Field(type = FieldType.Date) private LocalDateTime sentAt; //todo fix the date pattern so that you see correct data in kibana

    public static Message define(String sender, String receiver, String text, Instant time ){
        var sentAt = LocalDateTime.ofInstant(time, ZoneId.systemDefault());
        var id = UUID.randomUUID().toString();
        return new Message(id, sender, receiver, text, sentAt);
    }
}
//todo use @GeoPoint Point location, or GeoJsonPoint location,
