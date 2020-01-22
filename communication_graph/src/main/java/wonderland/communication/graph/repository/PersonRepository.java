package wonderland.communication.graph.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import wonderland.communication.graph.domain.Person;

@Repository
public interface PersonRepository extends Neo4jRepository<Person, String> {
}
