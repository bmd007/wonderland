package wonderland.webauthn.webauthnserver.dto;

public class StartRegistrationResponse {
    public final boolean success = true;
    //todo all that matters is this request
    public final RegistrationRequest request;
    public final StartRegistrationActions actions = new StartRegistrationActions();
    public StartRegistrationResponse(RegistrationRequest request) {
        this.request = request;
    }
}