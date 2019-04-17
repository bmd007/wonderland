package ir.tiroon.microservices

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
class MicrosevicesApplication {

    //TODO add cassandra as a docker compose service
    //or find a easy way to use a embeddedCassandra
    //or just gave up on using cassandra for now

    //TODO redefine domain to contain Command classes

    //TODO reconstruct httpHandlers

    static void main(String[] args) {
        SpringApplication.run MicrosevicesApplication, args
    }

}
