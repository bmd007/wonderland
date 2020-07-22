package ir.tiroon.microservices;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

import java.net.InetAddress;

@EnableConfigServer
@SpringBootApplication
public class ConfigCenterApplication implements CommandLineRunner {
	public static void main(String[] args) {
		SpringApplication.run(ConfigCenterApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
			InetAddress inetAddress = InetAddress.getLocalHost();
			System.out.println("IP Address :- " + inetAddress.getHostAddress());
			System.out.println("Host Name :- " + inetAddress.getHostName());
	}
}
