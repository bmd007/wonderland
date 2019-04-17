package it.tiroon.microservices.gateway

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

//@EnableWebFluxSecurity
@SpringBootApplication
class GatewayApplication {

	//TODO consider using RSocket for video streaming

//	@Bean
	SecurityWebFilterChain authorization(ServerHttpSecurity security){
		security
			.authorizeExchange().pathMatchers("/secured").authenticated()
			.anyExchange().permitAll()
			.and().httpBasic()
			.and().build()
	}

	@Bean
	DiscoveryClientRouteDefinitionLocator definitionLocator(DiscoveryClient discoveryClient){
		return new DiscoveryClientRouteDefinitionLocator(discoveryClient)
	}

	static void main(String[] args) {
		SpringApplication.run(GatewayApplication, args)
	}

}
