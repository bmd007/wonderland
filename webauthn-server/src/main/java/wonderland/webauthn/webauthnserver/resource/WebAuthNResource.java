package wonderland.webauthn.webauthnserver.resource;

import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.extension.appid.InvalidAppIdException;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wonderland.webauthn.webauthnserver.dto.AssertionResponse;
import wonderland.webauthn.webauthnserver.dto.RegistrationResponse;
import wonderland.webauthn.webauthnserver.dto.StartAuthenticationResponse;
import wonderland.webauthn.webauthnserver.dto.StartRegistrationResponse;
import wonderland.webauthn.webauthnserver.dto.SuccessfulAuthenticationResult;
import wonderland.webauthn.webauthnserver.dto.SuccessfulRegistrationResult;
import wonderland.webauthn.webauthnserver.service.WebAuthNService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Slf4j
@RestController
public class WebAuthNResource {

    private final WebAuthNService server;

    public WebAuthNResource(WebAuthNService server) {
        this.server = server;
    }

    private static final class Index {
        public final URL authenticate;
        public final URL deleteAccount;
        public final URL deregister;
        public final URL register;

        public Index() throws MalformedURLException {
            authenticate = new URL("https://localhost.localdomain/authenticate");
            deleteAccount = new URL("https://localhost.localdomain/delete-account");
            deregister = new URL("https://localhost.localdomain/action/deregister");
            register = new URL("https://localhost.localdomain/register");
        }
    }

    private static final class Info {
        public final URL version;

        public Info() throws MalformedURLException {
            version = new URL("https://localhost.localdomain/version");
        }
    }

    private static final class IndexResponse {
        public final Index actions = new Index();
        public final Info info = new Info();

        private IndexResponse() throws MalformedURLException {
        }
    }

    @GetMapping("/actions")
    public IndexResponse index() throws IOException {
        return new IndexResponse();
    }

    @PostMapping(value = "register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public StartRegistrationResponse startRegistration(
            @NotBlank @RequestParam("username") String username,
            @NotBlank @RequestParam("displayName") String displayName,
            @NotBlank @RequestParam("credentialNickname") String credentialNickname,
            @RequestParam(value = "requireResidentKey", required = false, defaultValue = "required") String requireResidentKey)
            throws InvalidAppIdException {
        var registrationRequest = server.startRegistration(
                username,
                displayName,
                Optional.ofNullable(credentialNickname),
                requireResidentKey.equals("required") ? ResidentKeyRequirement.REQUIRED : ResidentKeyRequirement.PREFERRED
        );
        return new StartRegistrationResponse(registrationRequest);
    }

    record RegisterRequestBody(@NotBlank String username, @NotBlank String displayName, @NotBlank String credentialNickname, String requireResidentKey) { }

    @PostMapping( "register/json")
    public StartRegistrationResponse startRegistration(@RequestBody RegisterRequestBody requestBody) throws InvalidAppIdException {
        ResidentKeyRequirement residentKeyRequirement = Optional.ofNullable(requestBody.requireResidentKey)
                .filter(s -> s.equals("required"))
                .map(s -> ResidentKeyRequirement.REQUIRED)
                .orElseGet(() -> ResidentKeyRequirement.PREFERRED);
        return startRegistration(
                requestBody.username,
                requestBody.displayName,
                requestBody.credentialNickname,
                residentKeyRequirement.getValue()
        );
    }

    @PostMapping("hi")
    public RegistrationResponse hi(@NotBlank RegistrationResponse registrationResponse) {
        return registrationResponse;
    }

    @PostMapping("register/finish")
    public SuccessfulRegistrationResult finishRegistration(@NotBlank RegistrationResponse registrationResponse) {
        log.info("finishRegistration: {}", registrationResponse);
        return server.finishRegistration(registrationResponse);
    }

    @PostMapping(value = "authenticate", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public StartAuthenticationResponse startAuthentication(@NotBlank @RequestParam("username") String username) throws MalformedURLException {
        log.info("startAuthentication username: {}", username);
        var assertionRequestWrapper = server.startAuthentication(username);
        return new StartAuthenticationResponse(assertionRequestWrapper);
    }

    @PostMapping("authenticate/finish")
    public SuccessfulAuthenticationResult finishAuthentication(@NonNull AssertionResponse assertionResponse) {
        log.info("finishAuthentication assertionResponse: {}", assertionResponse);
        return server.finishAuthentication(assertionResponse);
    }
}
