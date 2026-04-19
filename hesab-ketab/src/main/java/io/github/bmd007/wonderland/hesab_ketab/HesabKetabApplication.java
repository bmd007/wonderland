package io.github.bmd007.wonderland.hesab_ketab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class HesabKetabApplication {

	static void main(String[] args) {
		SpringApplication.run(HesabKetabApplication.class, args);
	}

}
