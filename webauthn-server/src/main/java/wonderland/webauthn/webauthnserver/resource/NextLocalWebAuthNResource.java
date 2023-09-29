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
import wonderland.webauthn.webauthnserver.dto.*;
import wonderland.webauthn.webauthnserver.service.WebAuthNService;

import java.util.Optional;

@CrossOrigin(origins = {"https://localhost.localdomain:8081",
        "https://localhost.localdomain:8080",
        "https://localhost.localdomain"}
        , originPatterns = {"https://*.zoo.wonderland"})
@Slf4j
@RestController
@RequestMapping("/v1/methods/webauthn/")
public class WebAuthNResource {

    private final WebAuthNService server;

    public WebAuthNResource(WebAuthNService server) {
        this.server = server;
    }

    record RegisterRequestBody(@NotBlank String displayName, @NotBlank String credentialNickname) {
    }

    @DeleteMapping
    public void deleteAll() {
        server.deleteAll();
    }

    @PostMapping("register")
    public RegistrationRequest startRegistration(@RequestBody RegisterRequestBody registerRequest) {
        return server.startRegistration(
                username,
                registerRequest.displayName,
                Optional.ofNullable(registerRequest.credentialNickname),
                country,
                AuthenticatorAttachment.PLATFORM);
    }

    @PostMapping("register/cross-platform")
    public RegistrationRequest startRegistrationCrossPlatform(@RequestBody RegisterRequestBody registerRequest) {
        return server.startRegistration(
                username,
                registerRequest.displayName,
                Optional.ofNullable(registerRequest.credentialNickname),
                country,
                AuthenticatorAttachment.CROSS_PLATFORM);
    }

    @PostMapping("register/local")
    public RegistrationRequest startRegistrationLocal(@RequestBody RegisterRequestBody registerRequest) {
        return startRegistration(registerRequest, hardcoded-username, hardcoded-country);
    }

    @PostMapping("register/local/cross-platform")
    public RegistrationRequest startRegistrationLocalCrossPlatform(@RequestBody RegisterRequestBody registerRequest) {
        return startRegistrationCrossPlatform(registerRequest, hardcoded-username, hardcoded-country);
    }

    @PostMapping("register/finish")
    public SuccessfulRegistrationResult finishRegistration(@RequestBody RegistrationResponse registrationResponse) {
        return server.finishRegistration(registrationResponse, country);
    }

    @PostMapping("register/finish/local")
    public SuccessfulRegistrationResult finishRegistration(@RequestBody RegistrationResponse registrationResponse) {
        return finishRegistration(registrationResponse, hardcoded-country);
    }

    record AuthenticateRequestBody(@Nullable String username,
                                   @NotBlank String countryCode,
                                   @Nullable ByteArray userHandle) {
    }

    @PostMapping(value = "authenticate")
    public AssertionRequestWrapper startAuthentication(@RequestBody AuthenticateRequestBody authenticateRequestBody) {
        String countryCode = authenticateRequestBody.countryCode();
        return Optional.ofNullable(authenticateRequestBody.username())
                .map(username -> server.startAuthentication(username, countryCode))
                .orElseGet(() ->
                        Optional.ofNullable(authenticateRequestBody.userHandle)
                                .map(userHandle -> server.startAuthentication(userHandle, countryCode))
                                .orElseGet(() -> server.startAuthentication(countryCode))
                );
    }

    @PostMapping(value = "authenticate/finish")
    public SuccessfulAuthenticationResult finishAuthentication(@RequestBody AssertionResponse assertionResponse,
                                                               @RequestParam(name = "country") String countryCode) {
        return server.finishAuthentication(assertionResponse, countryCode);
    }
}
