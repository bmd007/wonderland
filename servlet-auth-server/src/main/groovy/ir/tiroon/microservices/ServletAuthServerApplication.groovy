package ir.tiroon.microservices

import ir.tiroon.microservices.model.oauth2.Oauth2Client
import ir.tiroon.microservices.model.userManagement.Role
import ir.tiroon.microservices.service.OauthClientDetailServices
import ir.tiroon.microservices.service.RoleServices
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.provider.ClientDetailsService

@SpringBootApplication
class ServletAuthServerApplication implements CommandLineRunner {

    static void main(String[] args) {
        SpringApplication.run ServletAuthServerApplication, args
    }


    @Autowired
    RoleServices roleServices

//    @Autowired
//    OauthClientDetailServices clientDetailsService


    @Override
    void run(String... args) throws Exception {

        if (roleServices.getByName('USER') == null) {
            def r1 = new Role()
            r1.setRoleName('USER')
            r1.setDescription('Common system users')
            roleServices.save(r1)

        }

        if (roleServices.getByName('ADMIN') == null) {

            def r2 = new Role()
            r2.setRoleName('ADMIN')
            r2.setDescription('Administrators')

            roleServices.save(r2)
        }

//        if(clientDetailsService.getAndNullIfNotFound("person-command")){
//            clientDetailsService.persist("person-command","person-secret")
//        }
    }

}
