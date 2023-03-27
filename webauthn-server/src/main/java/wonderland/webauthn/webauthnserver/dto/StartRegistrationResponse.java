package wonderland.webauthn.webauthnserver.dto;

public class StartRegistrationResponse {
    public final boolean success = true;
    public final RegistrationRequest request;
    public final StartRegistrationActions actions = new StartRegistrationActions();

    public StartRegistrationResponse(RegistrationRequest request) {
        this.request = request;
    }
}