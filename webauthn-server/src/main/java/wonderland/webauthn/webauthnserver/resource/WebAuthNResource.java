package wonderland.webauthn.webauthnserver.resource;

import com.yubico.webauthn.data.AuthenticatorAttachment;
import com.yubico.webauthn.data.ByteArray;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wonderland.webauthn.webauthnserver.dto.AssertionRequestWrapper;
import wonderland.webauthn.webauthnserver.dto.AssertionResponse;
import wonderland.webauthn.webauthnserver.dto.RegistrationRequest;
import wonderland.webauthn.webauthnserver.dto.RegistrationResponse;
import wonderland.webauthn.webauthnserver.dto.SuccessfulAuthenticationResult;
import wonderland.webauthn.webauthnserver.dto.SuccessfulRegistrationResult;
import wonderland.webauthn.webauthnserver.service.WebAuthNService;

import java.util.Optional;

@CrossOrigin(origins = {
        "https://localhost.localdomain",
        "http://localhost",
        "https://localhost",
}
        , originPatterns = {"https://*.staging.wonderland.se"})
@Slf4j
@RestController
@RequestMapping("/v1/methods/webauthn/")
public class WebAuthNResource {

    private final WebAuthNService server;

    public WebAuthNResource(WebAuthNService server) {
        this.server = server;
    }

    public record RegisterRequestBody(@NotBlank String displayName, @NotBlank String credentialNickname) {
    }

    @DeleteMapping
    public void deleteAll() {
        server.deleteAll();
    }

    @PostMapping("register")
    public RegistrationRequest startRegistration(@RequestBody RegisterRequestBody registerRequest, String validatedUsername) {
        return server.startRegistration(
                validatedUsername,
                registerRequest.displayName,
                Optional.ofNullable(registerRequest.credentialNickname),
                AuthenticatorAttachment.PLATFORM);
    }

    @PostMapping("register/cross-platform")
    public RegistrationRequest startRegistrationCrossPlatform(@RequestBody RegisterRequestBody registerRequest, String validatedUsername) {
        return server.startRegistration(
                validatedUsername,
                registerRequest.displayName,
                Optional.ofNullable(registerRequest.credentialNickname),
                AuthenticatorAttachment.CROSS_PLATFORM);
    }

    @PostMapping("register/local")
    public RegistrationRequest startRegistrationLocal(@RequestBody RegisterRequestBody registerRequest) {
        return startRegistration(registerRequest, "007123e0-a0c3-4126-97d2-f7c7542a3228");
    }

    @PostMapping("register/local/cross-platform")
    public RegistrationRequest startRegistrationLocalCrossPlatform(@RequestBody RegisterRequestBody registerRequest) {
        return startRegistrationCrossPlatform(registerRequest, "007123e0-a0c3-4126-97d2-f7c7542a3228");
    }

    @PostMapping("register/finish")
    public SuccessfulRegistrationResult finishRegistration(@RequestBody RegistrationResponse registrationResponse) {
        return server.finishRegistration(registrationResponse);
    }

    public record AuthenticateRequestBody(@Nullable String username, @NotBlank String countryCode, @Nullable ByteArray userHandle) {
    }

    @PostMapping(value = "authenticate")
    public AssertionRequestWrapper startAuthentication(@RequestBody AuthenticateRequestBody authenticateRequestBody) {
        return Optional.ofNullable(authenticateRequestBody.username())
                .map(server::startAuthentication)
                .orElseGet(() ->
                        Optional.ofNullable(authenticateRequestBody.userHandle)
                                .map(server::startAuthentication)
                                .orElseGet(server::startAuthentication)
                );
    }

    @PostMapping(value = "authenticate/finish")
    public SuccessfulAuthenticationResult finishAuthentication(@RequestBody AssertionResponse assertionResponse,
                                                               @RequestParam(name = "country") String countryCode) {
        return server.finishAuthentication(assertionResponse);
    }
}
