package wonderland.webauthn.webauthnserver.dto;

import lombok.Value;

@Value
public class StartAuthenticationActions {
    String finish = "http://local.next.test.nordnet.fi:9568/register/finish";
}
