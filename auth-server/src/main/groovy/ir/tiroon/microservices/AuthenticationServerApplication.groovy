package ir.tiroon.microservices

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class AuthenticationServerApplication {
//	https://projects.spring.io/spring-security-oauth/docs/oauth2.html

	//continue using https://docs.spring.io/spring-security/site/docs/current/reference/html5/#webflux-oauth2-resource-server
	//and https://github.com/spring-projects/spring-security/tree/5.1.5.RELEASE/samples/boot/oauth2resourceserver-webflux
	static void main(String[] args) {
		SpringApplication.run AuthenticationServerApplication, args
	}
}
