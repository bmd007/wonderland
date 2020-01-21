package wonderland.communication.graph.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories("wonderland.communication.graph.repository")
@EntityScan(basePackages = "wonderland.communication.graph.domain")
public class Neo4jConfiguration {
}
