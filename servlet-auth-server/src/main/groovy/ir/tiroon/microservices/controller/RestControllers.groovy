package ir.tiroon.microservices.controller


import ir.tiroon.microservices.model.userManagement.User
import ir.tiroon.microservices.service.OauthClientDetailServices
import ir.tiroon.microservices.service.UserServices
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.Principal

@RestController
class RestControllers {

    @Autowired
    UserServices userServices

    @GetMapping("/users")
    @ResponseBody
    List<User> users() {
        userServices.list()
    }


    @GetMapping(path = '/admin')
    ResponseEntity admin(Principal principal) {
        new ResponseEntity("Admin is " + principal.getName(), HttpStatus.OK)
    }

    //this is not so secure
    @GetMapping(path = '/user')
    Principal user(Principal principal) {
        principal
    }

    @Autowired
    OauthClientDetailServices clientDetailServices

    @GetMapping(path = '/new/client/{id}/{secret}')
    ResponseEntity register(@PathVariable("id") String id, @PathVariable("secret") String secret) {
        clientDetailServices.persist(id, secret)
        new ResponseEntity(HttpStatus.OK)
    }
}

