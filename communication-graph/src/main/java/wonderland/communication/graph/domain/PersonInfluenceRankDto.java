package wonderland.communication.graph.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class PersonInfluenceRankDto {
    private String email;
    private float score;

    public PersonInfluenceRankDto() {
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("email", email)
                .add("rank", score)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonInfluenceRankDto that = (PersonInfluenceRankDto) o;
        return Float.compare(that.score, score) == 0 &&
                Objects.equal(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email, score);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @JsonCreator
    public PersonInfluenceRankDto(@JsonProperty("email") String email, @JsonProperty("rank") float score) {
        this.email = email;
        this.score = score;
    }
}
