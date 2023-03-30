package wonderland.webauthn.webauthnserver.dto;

import lombok.Value;

@Value
public class StartRegistrationActions {
    String finish = "https://localhost.localdomain/register/finish";
}