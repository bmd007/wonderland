package wonderland.webauthn.webauthnserver;

import com.yubico.webauthn.data.ResidentKeyRequirement;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wonderland.webauthn.webauthnserver.dto.RegistrationResponse;
import wonderland.webauthn.webauthnserver.dto.StartRegistrationResponse;
import wonderland.webauthn.webauthnserver.dto.SuccessfulRegistrationResult;

import java.net.MalformedURLException;
import java.util.Optional;

@Slf4j
@RestController
public class WebAuthNResource {

    final private WebAuthNService server;

    public WebAuthNResource(WebAuthNService server) {
        this.server = server;
    }

    @PostMapping(value = "register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public StartRegistrationResponse startRegistration(
            @NotBlank @RequestParam("username") String username,
            @NotBlank @RequestParam("displayName") String displayName,
            @RequestParam("credentialNickname") String credentialNickname,
            @RequestParam(value = "requireResidentKey", required = false) boolean requireResidentKey)
            throws MalformedURLException {
        var registrationRequest = server.startRegistration(
                username,
                displayName,
                Optional.ofNullable(credentialNickname),
                requireResidentKey ? ResidentKeyRequirement.REQUIRED : ResidentKeyRequirement.DISCOURAGED
        );
        return new StartRegistrationResponse(registrationRequest);
    }

    @PostMapping("register/finish")
    public SuccessfulRegistrationResult finishRegistration(@NotBlank RegistrationResponse registrationResponse) {
        log.trace("finishRegistration responseJson: {}", registrationResponse);
        return server.finishRegistration(registrationResponse);
    }

}
