package wonderland.communication.graph.domain;


import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

//this class represents nodes on graph
@Value
@Builder
@Node("Person")
public final class Person {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    private String email;

    @Relationship(direction = OUTGOING, type = "SENT_MESSAGE_TO")
    private Set<Communication> communications;

    public static Person of(String email) {
        return Person.builder()
                .id(null)
                .version(0L)
                .email(email)
                .communications(new HashSet<>())
                .build();
    }

    public static Person of(CommunicationLessPersonProjection projection) {
        return Person.builder()
                .id(projection.id())
                .version(projection.version())
                .email(projection.email())
                .communications(new HashSet<>())
                .build();
    }

    public static Person of(CommunicationLessPersonProjection projection, Set<Communication> communications) {
        return Person.builder()
                .id(projection.id())
                .version(projection.version())
                .email(projection.email())
                .communications(communications)
                .build();
    }

    public Person.PersonBuilder cloneBuilder() {
        return Person.builder()
                .id(this.getId())
                .version(this.getVersion())
                .email(this.getEmail())
                .communications(this.getCommunications());
    }

    public Person addCommunication(Communication communication) {
        this.communications.add(communication);
        return this;
    }

    public Person addCommunications(Set<Communication> communications) {
        return cloneBuilder()
                .communications(communications)
                .build();
    }
}
