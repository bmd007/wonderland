package wonderland.webauthn.webauthnserver.dto;

import lombok.Value;

@Value
public class StartRegistrationActions {
    String finish = "http://local.next.test.nordnet.fi/register/finish";
}