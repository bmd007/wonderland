package wonderland.webauthn.webauthnserver.resource;

import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.extension.appid.InvalidAppIdException;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import wonderland.webauthn.webauthnserver.dto.*;
import wonderland.webauthn.webauthnserver.service.WebAuthNService;

import java.util.Optional;


@CrossOrigin(origins = {"https://localhost.localdomain:8080", "https://localhost.localdomain"})
@Slf4j
@RestController
@RequestMapping("next")
public class NextLocalWebAuthNResource {

    private final WebAuthNService server;

    public NextLocalWebAuthNResource(WebAuthNService server) {
        this.server = server;
    }

    record RegisterRequestBody(@NotBlank String username,
                               @NotBlank String displayName,
                               @NotBlank String credentialNickname,
                               String requireResidentKey) {
    }

    @PostMapping("register")
    public RegistrationRequest startRegistration(@RequestBody RegisterRequestBody requestBody) throws InvalidAppIdException {
        //todo investigate effects of residentKeyRequirement
        ResidentKeyRequirement residentKeyRequirement = Optional.ofNullable(requestBody.requireResidentKey)
                .filter("required"::equals)
                .map(s -> ResidentKeyRequirement.REQUIRED)
                .orElse(ResidentKeyRequirement.PREFERRED);
        return server.startRegistration(
                requestBody.username,
                requestBody.displayName,
                Optional.ofNullable(requestBody.credentialNickname),
                residentKeyRequirement);
    }

    @PostMapping("register/finish")
    public SuccessfulRegistrationResult finishRegistration(@RequestBody RegistrationResponse registrationResponse) {
        return server.finishRegistration(registrationResponse);
    }

    record AuthenticateRequestBody(@NotBlank String username) {
    }

    @PostMapping(value = "authenticate")
    public AssertionRequestWrapper startAuthentication(@RequestBody AuthenticateRequestBody authenticateRequestBody) throws InvalidAppIdException {
        return server.startAuthentication(authenticateRequestBody.username);
    }

    @PostMapping("authenticate/finish")
    public SuccessfulAuthenticationResult finishAuthentication(@RequestBody AssertionResponse assertionResponse) {
        return server.finishAuthentication(assertionResponse);
    }
}
