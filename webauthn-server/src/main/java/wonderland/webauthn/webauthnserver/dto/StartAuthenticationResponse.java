package wonderland.webauthn.webauthnserver.dto;

import java.net.MalformedURLException;
import java.net.URL;


public class StartAuthenticationResponse {
    public final boolean success = true;
    public final AssertionRequestWrapper request;
    public final StartAuthenticationActions actions = new StartAuthenticationActions();

    public StartAuthenticationResponse(AssertionRequestWrapper request)
            throws MalformedURLException {
        this.request = request;
    }

    class StartAuthenticationActions {
        URL finish = new URL("http://localhost:9568/authentication/finish");

        private StartAuthenticationActions() throws MalformedURLException {
        }
    }
}
