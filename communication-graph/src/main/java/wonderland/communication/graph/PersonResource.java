package wonderland.communication.graph;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import wonderland.communication.graph.dto.PersonInfluenceRankDto;

@RestController
public class PersonResource {

    private Neo4jClient client;

    public PersonResource(Neo4jClient client) {
        this.client = client;
    }

    @GetMapping("/most/influential/person")
    public PersonInfluenceRankDto getMostInfluentialPerson() {
        return client.query("""
                        CALL gds.pageRank.stream({
                            nodeProjection: 'Person',
                            relationshipProjection: 'SENT_MESSAGE_TO'
                        })
                        YIELD nodeId, score
                        MATCH (node) WHERE id(node) = nodeId
                        RETURN node.email AS email, score
                        ORDER BY score DESC
                        LIMIT 1""")
                .fetchAs(PersonInfluenceRankDto.class)
                .mappedBy((typeSystem, record) ->
                        new PersonInfluenceRankDto(record.get("email").asString(), record.get("score").asDouble()))
                .one()
                .get();
    }
}
