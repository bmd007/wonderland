package it.tiroon.microservices.gateway

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.reactive.PathRequest
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.client.RestTemplate

//@EnableReactiveMethodSecurity
//@EnableWebFluxSecurity
@EnableResourceServer
@SpringBootApplication
class GatewayApplication {

//	@LoadBalanced
//	@Bean
//	RestTemplate restTemplate() {
//		return new RestTemplate();
//	}

	//TODO consider using RSocket for video streaming
//	ReactiveJwtDecoder
	@Bean
	SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http
				.authorizeExchange()
				.anyExchange().authenticated()
				.and()

				.httpBasic().disable();

		return http.build();
	}

	@Bean
	DiscoveryClientRouteDefinitionLocator definitionLocator(DiscoveryClient discoveryClient){
		return new DiscoveryClientRouteDefinitionLocator(discoveryClient)
	}

	static void main(String[] args) {
		SpringApplication.run(GatewayApplication, args)
	}

}
