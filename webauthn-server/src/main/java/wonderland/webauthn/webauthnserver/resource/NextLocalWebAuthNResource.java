package se.nordnet.authentication.webauthn.resource;

import com.yubico.webauthn.data.AuthenticatorAttachment;
import com.yubico.webauthn.data.ByteArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.nordnet.authentication.webauthn.dto.AssertionRequestWrapper;
import se.nordnet.authentication.webauthn.dto.AssertionResponse;
import se.nordnet.authentication.webauthn.dto.RegistrationRequest;
import se.nordnet.authentication.webauthn.dto.RegistrationResponse;
import se.nordnet.authentication.webauthn.dto.SuccessfulAuthenticationResult;
import se.nordnet.authentication.webauthn.dto.SuccessfulRegistrationResult;
import se.nordnet.authentication.webauthn.service.WebAuthNService;
import se.nordnet.jwt.NordnetJWTClaims;
import se.nordnet.jwt.annotation.NordnetJwt;

import javax.validation.constraints.NotBlank;
import java.util.Optional;

@CrossOrigin(origins = {
        "https://localhost.localdomain:8081",
        "https://localhost.localdomain:8080",
        "https://localhost.localdomain",
        "https://local.next.test.nordnet.se:8080",
        "https://local.next.test.nordnet.se:8081",
        "https://local.next.test.nordnet.dk:8081",
        "https://local.next.test.nordnet.fi:8081",
        "https://local.next.test.nordnet.no:8081"
})
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

    private void validateAuthenticationMethod(NordnetJWTClaims nordnetJWTClaims) {
        final String authenticationMethod = nordnetJWTClaims.getAuthenticationMethodReference();
        if ("basic".equalsIgnoreCase(authenticationMethod) || "sms".equalsIgnoreCase(authenticationMethod)) {
            log.error("Invalid authentication: {} method at enrollment for customer: {}", authenticationMethod, nordnetJWTClaims.getSubject());
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid authentication method %s".formatted(authenticationMethod));
        }
    }

    @DeleteMapping
    public void deleteAll() {
        server.deleteAll();
    }

    @PostMapping("register")
    public RegistrationRequest startRegistration(@RequestBody RegisterRequestBody registerRequest,
                                                 @NordnetJwt NordnetJWTClaims nordnetJWTClaims) {
        validateAuthenticationMethod(nordnetJWTClaims);
        return server.startRegistration(
                nordnetJWTClaims.getSubject(),
                registerRequest.displayName,
                Optional.ofNullable(registerRequest.credentialNickname),
                nordnetJWTClaims.getOrganizationCountry(),
                AuthenticatorAttachment.PLATFORM);
    }

    @PostMapping("register/cross-platform")
    public RegistrationRequest startRegistrationCrossPlatform(@RequestBody RegisterRequestBody registerRequest,
                                                              @NordnetJwt NordnetJWTClaims nordnetJWTClaims) {
        validateAuthenticationMethod(nordnetJWTClaims);
        return server.startRegistration(
                nordnetJWTClaims.getSubject(),
                registerRequest.displayName,
                Optional.ofNullable(registerRequest.credentialNickname),
                nordnetJWTClaims.getOrganizationCountry(),
                AuthenticatorAttachment.CROSS_PLATFORM);
    }

    @PostMapping("register/local")
    public RegistrationRequest startRegistrationLocal(@RequestBody RegisterRequestBody registerRequest) {
        return startRegistration(registerRequest, NordnetJWTClaims.builder()
                .subject("007123e0-a0c3-4126-97d2-f7c7542a3228")
                .authenticationMethodReference("basic")
                .organizationCountry("se")
                .build());
    }

    @PostMapping("register/local/cross-platform")
    public RegistrationRequest startRegistrationLocalCrossPlatform(@RequestBody RegisterRequestBody registerRequest) {
        return startRegistrationCrossPlatform(registerRequest, NordnetJWTClaims.builder()
                .subject("007123e0-a0c3-4126-97d2-f7c7542a3228")
                .authenticationMethodReference("basic")
                .organizationCountry("se")
                .build());
    }

    @PostMapping("register/finish")
    public SuccessfulRegistrationResult finishRegistration(@RequestBody RegistrationResponse registrationResponse,
                                                           @NordnetJwt NordnetJWTClaims nordnetJWTClaims) {
        return server.finishRegistration(registrationResponse, nordnetJWTClaims.getOrganizationCountry());
    }

    @PostMapping("register/finish/local")
    public SuccessfulRegistrationResult finishRegistration(@RequestBody RegistrationResponse registrationResponse) {
        return finishRegistration(registrationResponse, NordnetJWTClaims.builder().organizationCountry("se").build());
    }

    record AuthenticateRequestBody(@Nullable String username, @NotBlank String countryCode, @Nullable ByteArray userHandle) {
    }

    @PostMapping(value = "authenticate")
    public AssertionRequestWrapper startAuthentication(@RequestBody AuthenticateRequestBody authenticateRequestBody) {
        String countryCode = authenticateRequestBody.countryCode();
        return Optional.ofNullable(authenticateRequestBody.username())
                .map(username -> server.startAuthentication(username, countryCode))
                .orElseGet(() -> server.startAuthentication(authenticateRequestBody.userHandle, countryCode));
    }

    @PostMapping(value = "authenticate/finish")
    public SuccessfulAuthenticationResult finishAuthentication(@RequestBody AssertionResponse assertionResponse, @RequestParam(name = "country") String countryCode) {
        log.info("finishAuthenticateRequestBodyString {}", StringUtils.normalizeSpace(assertionResponse.toString()));
        return server.finishAuthentication(assertionResponse, countryCode);
    }
}
