package ir.tiroon.microservices


import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.config.server.EnableConfigServer

@EnableConfigServer
@SpringBootApplication
class ConfigServerApplication {

	static void main(String[] args) {
		SpringApplication.run ir.tiroon.microservices.ConfigServerApplication, args
	}
}
