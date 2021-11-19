package wonderland.communication.graph.domain;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

//this class represents nodes on graph
@Node("Person")
public final class Person {

    @Id
    @GeneratedValue
    private Long id;

    @Property
    private String email;

    @Relationship(direction = OUTGOING, type = "SENT_MESSAGE_TO")
    private List<Communication> communications = new ArrayList<>();

    public static Person of(String email) {
        return new Person(null, email, new ArrayList<>());
    }

    public Person withCommunications(List<Communication> outwardCommunications) {
        if (this.communications != null && this.communications.equals(outwardCommunications)) {
            return this;
        } else {
            return new Person(id, this.email, outwardCommunications);
        }
    }

    public Person addCommunication(Communication communication) {
//        if (this.communications != null && !this.communications.contains(communication)) {
//            List<Communication> communications = Stream
//                    .concat(Stream.of(communication), this.communications.stream())
//                    .collect(Collectors.toList());
//            return withCommunications(communications);
//        } else if (this.communications == null ){
//            return withCommunications(List.of(communication));
//        }
        this.communications.add(communication);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCommunications(List<Communication> communications) {
        this.communications = communications;
    }

    public Person withId(Long id) {
        if (this.id != null && this.id.equals(id)) {
            return this;
        } else {
            return new Person(id, this.email, this.communications);
        }
    }

    public Person withEmail(String email) {
        if (this.id.equals(id)) {
            return this;
        } else {
            return new Person(this.id, email, this.communications);
        }
    }

    public Person() {
        id = null;
        email = null;
    }

    public Person(Long id, String email, List<Communication> communications) {
        this.id = id;
        this.email = email;
        this.communications = communications;
    }

    public Person(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public List<Communication> getCommunications() {
        return communications;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equal(id, person.id) && Objects.equal(email, person.email) && Objects.equal(communications, person.communications);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, email, communications);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("email", email)
                .add("outwardCommunications", communications)
                .toString();
    }
}
