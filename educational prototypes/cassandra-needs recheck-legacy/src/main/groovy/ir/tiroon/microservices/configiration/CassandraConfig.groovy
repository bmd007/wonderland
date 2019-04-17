package ir.tiroon.microservices.configiration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration
import org.springframework.data.cassandra.config.SchemaAction
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories

@Configuration
@EnableReactiveCassandraRepositories(basePackages = "ir.tiroon.microservices.personcommand")
class CassandraConfig extends AbstractReactiveCassandraConfiguration {

    @Value('${cassandra.contactpoints}')
    String contactPoints

    @Value('${cassandra.port}')
    int port

    @Value('${cassandra.keyspace}')
    String keyspace

    @Value('${cassandra.basepackages}')
    String basePackages

    @Override
    protected String getKeyspaceName() {
        keyspace
    }

    @Override
    protected String getContactPoints() {
        contactPoints
    }

    @Override
    protected int getPort() {
        port
    }

    @Override
    SchemaAction getSchemaAction() {
        SchemaAction.CREATE_IF_NOT_EXISTS
    }

    @Override
    String[] getEntityBasePackages() {
        [basePackages]
    }

}