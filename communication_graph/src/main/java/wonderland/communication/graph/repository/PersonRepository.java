package wonderland.communication.graph.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import wonderland.communication.graph.domain.Person;

@Repository
public interface PersonRepository extends Neo4jRepository<Person, String> {
//    CALL algo.pageRank.stream("Person", "SENT_MESSAGE", {iterations:20})
//    YIELD nodeId, score
//    MATCH (node) WHERE id(node) = nodeId
//    RETURN node.email AS page,score
//    ORDER BY score DESC
}
