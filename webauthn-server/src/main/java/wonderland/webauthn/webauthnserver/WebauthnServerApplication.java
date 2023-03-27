package wonderland.webauthn.webauthnserver;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.exception.Base64UrlException;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wonderland.webauthn.webauthnserver.data.RegistrationRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.yubico.webauthn.data.ByteArray.fromBase64Url;

@RestController
@SpringBootApplication
public class WebauthnServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebauthnServerApplication.class, args);
    }

    public class StartRegistrationActions {
        URL finish = new URL("http://localhost:9568/finish");

        StartRegistrationActions() throws MalformedURLException {
        }
    }

    class StartRegistrationResponse {
        public final boolean success = true;
        public final RegistrationRequest request;
        public final StartRegistrationActions actions = new StartRegistrationActions();

        private StartRegistrationResponse(RegistrationRequest request) throws MalformedURLException {
            this.request = request;
        }
    }


    @Autowired WebAuthNService server;

	@PostMapping(value = "register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public StartRegistrationResponse startRegistration(
            @NotBlank @RequestParam("username") String username,
            @NotBlank @RequestParam("displayName") String displayName,
            @RequestParam("credentialNickname") String credentialNickname,
            @RequestParam(value = "requireResidentKey", required = false) boolean requireResidentKey,
            @RequestParam("sessionToken") String sessionTokenBase64) throws MalformedURLException, ExecutionException {
        server.startRegistration(
                username,
                displayName,
                Optional.ofNullable(credentialNickname),
                requireResidentKey ? ResidentKeyRequirement.REQUIRED : ResidentKeyRequirement.DISCOURAGED,
                Optional.ofNullable(sessionTokenBase64)
                        .map(
                                base64 -> {
                                    try {
                                        return fromBase64Url(base64);
                                    } catch (Base64UrlException e) {
                                        throw new RuntimeException(e);
                                    }
                                }));

            return new StartRegistrationResponse();
    }

}
