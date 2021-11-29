package wonderland.communication.graph.domain;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

//this class represents nodes on graph
@Data
@Builder
@Node("Person")
public final class Person {

    @Id @GeneratedValue
    private Long id;

    @Version
    private Long version;

    private String email;

    @Relationship(direction = OUTGOING, type = "SENT_MESSAGE_TO")
    private List<Communication> communications;

    public static Person of(String email) {
        return Person.builder()
                .id(null)
                .version(0L)
                .email(email)
                .communications(new ArrayList<>())
                .build();
    }

    public Person addCommunication(Communication communication) {
        this.communications.add(communication);
        return this;
    }

}
