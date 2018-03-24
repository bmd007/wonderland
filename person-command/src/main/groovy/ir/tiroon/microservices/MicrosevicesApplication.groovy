package ir.tiroon.microservices

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync

//@EnableWebFluxSecurity
@EnableAsync
@Configuration
@SpringBootApplication
class MicrosevicesApplication {

    static void main(String[] args) {
        SpringApplication.run MicrosevicesApplication, args
    }

}
