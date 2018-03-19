package ir.tiroon.microservices.ms11.config

import org.springframework.beans.factory.annotation.Value

//@Configuration
//@EnableReactiveCassandraRepositories
class CassandraConfig {
//    extends AbstractReactiveCassandraConfiguration {

    @Value('${cassandra.contactpoints}')
    private String contactPoints

    @Value('${cassandra.port}')
    private int port

    @Value('${cassandra.keyspace}')
    private String keyspace

    @Value('${cassandra.basepackages}')
    private String basePackages




//    @Override
//    protected String getKeyspaceName() {
//        return keyspace
//    }
//
//    @Override
//    protected String getContactPoints() {
//        return contactPoints
//    }
//
//    @Override
//    protected int getPort() {
//        return port
//    }
//
//    @Override
//    SchemaAction getSchemaAction() {
//        return SchemaAction.CREATE_IF_NOT_EXISTS
//    }
//
//    @Override
//    String[] getEntityBasePackages() {
//        return [basePackages]
//    }

}