package wonderland.communication.graph.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.typeconversion.DateString;

import java.time.Instant;
import java.time.LocalDateTime;

//this class represent edges on graph
public class Communication {

    @Id @GeneratedValue String id;
    @StartNode Person from;
    @EndNode Person to;

    @DateString @Property Instant time;

    public Communication(Person from, Person to, Instant time) {
        this.from = from;
        this.to = to;
        this.time = time;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("from", from)
                .add("to", to)
                .add("time", time)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Communication that = (Communication) o;
        return  Objects.equal(from, that.from) &&
                Objects.equal(to, that.to) &&
                Objects.equal(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(from, to, time);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Person getFrom() {
        return from;
    }

    public void setFrom(Person from) {
        this.from = from;
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

    public Communication() {
    }
}
