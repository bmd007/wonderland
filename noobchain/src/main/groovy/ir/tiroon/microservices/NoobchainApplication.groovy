package ir.tiroon.microservices

import com.fasterxml.jackson.databind.ObjectMapper
import ir.tiroon.microservices.model.Block
import ir.tiroon.microservices.model.NoobChain
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class NoobchainApplication implements CommandLineRunner{

	static void main(String[] args) {
		SpringApplication.run NoobchainApplication, args
	}


    @Override
    void run(String... args) throws Exception {
        def nb = new NoobChain()
    }


}
