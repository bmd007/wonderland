package wonderland.message.search.domain;


import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Document(indexName = "message_history")
public record Message (
    @Id  String id,
    @Field(type = FieldType.Text) String sender,
    @Field(type = FieldType.Text) String receiver,
    @Field(type = FieldType.Text) String text,
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") LocalDateTime sentAt
) {

    public static Message of(String sender, String receiver, String text, Instant time ){
        var sentAt = LocalDateTime.ofInstant(time, ZoneId.of("UTC"));
        var id = UUID.randomUUID().toString();
        return new Message(id, sender, receiver, text, sentAt);
    }
}
//todo use @GeoPoint Point location, or GeoJsonPoint location,
