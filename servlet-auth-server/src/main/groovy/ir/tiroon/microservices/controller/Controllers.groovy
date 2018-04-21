package ir.tiroon.microservices.controller

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by Lenovo on 01/07/2016.
 */

@Controller
class Controllers {


    @RequestMapping(method = RequestMethod.GET, path = "/login")
    String login(Model model) {
        SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken ? "/login" : "redirect:/index.html"
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication()
        if (auth != null) new SecurityContextLogoutHandler().logout(request, response, auth)
        "redirect:/login?logout"
    }


    @RequestMapping(value = "/accessDenied", method = RequestMethod.GET)
    String accessDeniedPage(ModelMap model) {
        model.addAttribute("user", getPrincipal())
        "/accessDenied"
    }

    private String getPrincipal() {
        String userName = null
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal()

        if (principal instanceof UserDetails) {
            userName = ((UserDetails) principal).getUsername()
        } else {
            userName = principal.toString()
        }
        userName
    }


}
