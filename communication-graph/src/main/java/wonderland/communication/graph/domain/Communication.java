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

    @Id @GeneratedValue
    private final Long id;

    @TargetNode
    private final Person to;

    @TargetNode
    private final Person from;

    @Property
    private Instant time;

    public Communication withId(Long id) {
        if (this.id != null && this.id.equals(id)) {
            return this;
        } else {
            return new Communication(id, this.from, this.to, this.time);
        }
    }

    public Communication withTo(Person to) {
        if (this.to != null && this.to.equals(to)) {
            return this;
        } else {
            return new Communication(this.id, this.from, to, this.time);
        }
    }

    public Communication withFrom(Person from) {
        if (this.from != null && this.from.equals(from)) {
            return this;
        } else {
            return new Communication(this.id, from, this.to, this.time);
        }
    }

    public static Communication defineNew(Person from, Person to, Instant time) {
        return new Communication(null, from, to, time);
    }

    public Communication() {
        id = null;
        to = null;
        from = null;
        time = null;
    }

    public Communication(Long id, Person from, Person to, Instant time) {
        this.id = id;
        this.to = to;
        this.from = from;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public Person getTo() {
        return to;
    }

    public Person getFrom() {
        return from;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Communication that = (Communication) o;
        return Objects.equal(id, that.id) && Objects.equal(to, that.to) && Objects.equal(from, that.from) && Objects.equal(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, to, from, time);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("to", to)
                .add("from", from)
                .add("time", time)
                .toString();
    }
}
