package wonderland.webauthn.webauthnserver;

import com.yubico.webauthn.data.ResidentKeyRequirement;
import demo.webauthn.WebAuthnRestResource;
import demo.webauthn.WebAuthnServer;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wonderland.webauthn.webauthnserver.dto.AssertionRequestWrapper;
import wonderland.webauthn.webauthnserver.dto.AssertionResponse;
import wonderland.webauthn.webauthnserver.dto.RegistrationResponse;
import wonderland.webauthn.webauthnserver.dto.StartAuthenticationResponse;
import wonderland.webauthn.webauthnserver.dto.StartRegistrationResponse;
import wonderland.webauthn.webauthnserver.dto.SuccessfulAuthenticationResult;
import wonderland.webauthn.webauthnserver.dto.SuccessfulRegistrationResult;

import java.net.MalformedURLException;
import java.util.List;
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


    @PostMapping(value = "authenticate", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public StartAuthenticationResponse startAuthentication(@NotBlank @RequestParam("username") String username) throws MalformedURLException {
        log.trace("startAuthentication username: {}", username);
        var assertionRequestWrapper = server.startAuthentication(username);
        return new StartAuthenticationResponse(assertionRequestWrapper);
    }

    @PostMapping("authenticate/finish")
    public SuccessfulAuthenticationResult finishAuthentication(@NonNull AssertionResponse assertionResponse) {
        log.trace("finishAuthentication assertionResponse: {}", assertionResponse);
        return server.finishAuthentication(assertionResponse);
    }
}
