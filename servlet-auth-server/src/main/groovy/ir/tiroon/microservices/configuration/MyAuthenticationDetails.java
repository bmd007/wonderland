package ir.tiroon.microservices.configuration;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * this class stores data used for authentication (mainly the authentication
 * method)
 */
public class MyAuthenticationDetails extends WebAuthenticationDetails {

    private static final long serialVersionUID = 1L;
    private String method;

    public void setMethod(String method) {
        this.method = method;
    }

    public MyAuthenticationDetails(HttpServletRequest request) {
        super(request);
        method = request.getParameter("method");
    }

    public String getMethod() {
        return method;
    }

}
