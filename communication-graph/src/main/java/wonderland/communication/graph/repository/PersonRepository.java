package wonderland.communication.graph.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import wonderland.communication.graph.domain.CommunicationLessPersonProjection;
import wonderland.communication.graph.domain.Person;
import wonderland.communication.graph.dto.PersonInfluenceRankDto;

import java.util.Optional;

@Transactional
@Repository
public interface PersonRepository extends Neo4jRepository<Person, Long> {

    @Query("""
            CALL gds.pageRank.stream({
                nodeProjection: 'Person',
                relationshipProjection: 'SENT_MESSAGE_TO'
            })
            YIELD nodeId, score
            MATCH (node) WHERE id(node) = nodeId
            RETURN node.email AS email, score
            ORDER BY score DESC
            LIMIT 1""")
    PersonInfluenceRankDto getInfluenceRank();

//    @Query("""
//            YIELD node
//            MATCH (node) WHERE node.email =: email
//            RETURN node.email AS email, node.id as id, node.version as version""")
    Optional<CommunicationLessPersonProjection> getPersonByEmail(String email);

    Optional<Person> findByEmail(String email);
}
