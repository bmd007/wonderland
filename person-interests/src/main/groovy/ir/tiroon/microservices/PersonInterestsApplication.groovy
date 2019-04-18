package ir.tiroon.microservices

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

//TODO ?
//@EnableReactiveMongoRepositories
@SpringBootApplication
class PersonInterestsApplication {

	static void main(String[] args) {
		SpringApplication.run PersonInterestsApplication, args
	}
}
