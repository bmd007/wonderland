module your.module {

    requires org.neo4j.cypherdsl.core;

    requires spring.data.commons;
    requires spring.data.neo4j;

    opens wonderland.communication.graph to spring.core;

    exports wonderland.communication.graph;
}