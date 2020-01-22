package wonderland.communication.graph.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;

import static org.neo4j.ogm.annotation.Relationship.INCOMING;
import static org.neo4j.ogm.annotation.Relationship.OUTGOING;

//this class represents nodes on graph
@NodeEntity("Person")
public class Person {

    @Id private String email;

    @Relationship(direction = OUTGOING)
    private List<Communication> outwardCommunications;

    @Relationship(direction = INCOMING)
    private List<Communication> inwardCommunications;

    public Person(String email) {
        this.email = email;
    }

    public Person() {

    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equal(email, person.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("email", email)
                .toString();
    }
}
