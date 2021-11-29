package wonderland.communication.graph.domain;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.Instant;

//this class represent edges on graph
@RelationshipProperties
public final class Communication {

    @Id
    @GeneratedValue
    private Long id;

    @TargetNode
    private Person to;

    @Property
    private Instant time;

    public Communication(Long id, Person to, Instant time) {
        this.id = id;
        this.to = to;
        this.time = time;
    }

    public static Communication toward(Person to) {
        return new Communication(null, to, Instant.now());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("to", to)
                .add("time", time)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Communication that = (Communication) o;
        return Objects.equal(id, that.id) && Objects.equal(to, that.to) && Objects.equal(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, to, time);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getTo() {
        return to;
    }

    public void setTo(Person to) {
        this.to = to;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}
