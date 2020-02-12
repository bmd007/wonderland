package ir.tiroon.microservices.serviceregistry

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@EnableEurekaServer
@SpringBootApplication
class ServiceRegistryApplication implements CommandLineRunner{

	static void main(String[] args) {
		SpringApplication.run(ServiceRegistryApplication, args)
	}

	@Override
	void run(String... args) throws Exception {
		InetAddress inetAddress = InetAddress.getLocalHost();
		System.out.println("IP Address:- " + inetAddress.getHostAddress());
		System.out.println("Host Name:- " + inetAddress.getHostName());
	}

}
