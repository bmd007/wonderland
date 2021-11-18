package wonderland.communication.graph.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import wonderland.communication.graph.domain.Communication;

@Repository
public interface CommunicationRepository extends Neo4jRepository<Communication, Long> {
}
