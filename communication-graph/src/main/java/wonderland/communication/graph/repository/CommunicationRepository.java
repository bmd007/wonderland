package wonderland.communication.graph.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import wonderland.communication.graph.domain.Communication;
import wonderland.communication.graph.domain.CommunicationLessPersonProjection;
import wonderland.communication.graph.domain.Person;
import wonderland.communication.graph.domain.PersonInfluenceScoreProjection;

import java.util.Optional;
import java.util.Set;


@Repository
public interface CommunicationRepository extends Neo4jRepository<Communication, Long> {
    Set<Communication> findAllByTo(Person to);
}
