package wonderland.webauthn.webauthnserver.dto;

import java.net.MalformedURLException;
import java.net.URL;

public class StartRegistrationResponse {
    public final boolean success = true;
    public final RegistrationRequest request;
    public final StartRegistrationActions actions = new StartRegistrationActions();

    public StartRegistrationResponse(RegistrationRequest request) throws MalformedURLException {
        this.request = request;
    }

    class StartRegistrationActions {
        URL finish = new URL("http://localhost:9568/register/finish");
        StartRegistrationActions() throws MalformedURLException {
        }
    }
}