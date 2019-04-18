package ir.tiroon.microservices

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class AuthenticationServerApplication {
//	https://projects.spring.io/spring-security-oauth/docs/oauth2.html
	static void main(String[] args) {
		SpringApplication.run AuthenticationServerApplication, args
	}
}
