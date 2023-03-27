package wonderland.webauthn.webauthnserver.dto;

public class StartAuthenticationResponse {
    public final boolean success = true;
    public final AssertionRequestWrapper request;
    public final StartAuthenticationActions actions = new StartAuthenticationActions();

    public StartAuthenticationResponse(AssertionRequestWrapper request) {
        this.request = request;
    }
}
