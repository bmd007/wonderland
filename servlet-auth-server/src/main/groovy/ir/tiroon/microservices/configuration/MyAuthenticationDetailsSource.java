package ir.tiroon.microservices.configuration;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * This class provides a custom AuthenticationDetail which stores the connection
 * type method (Oracle/Ldap)
 *
 */
public class MyAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> {

    /**
     * returns the {@link WebAuthenticationDetails} according to LuxFact rule
     * 
     * @param context
     */
    public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new MyAuthenticationDetails(context);
    }

}
