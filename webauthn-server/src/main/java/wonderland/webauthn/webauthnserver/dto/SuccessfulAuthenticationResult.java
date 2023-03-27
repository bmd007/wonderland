package wonderland.webauthn.webauthnserver.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yubico.webauthn.data.AuthenticatorData;
import com.yubico.webauthn.data.ByteArray;
import demo.webauthn.WebAuthnServer;
import lombok.AllArgsConstructor;
import lombok.Value;
import wonderland.webauthn.webauthnserver.domain.CredentialRegistration;

import java.util.Collection;

@Value
@AllArgsConstructor
public class SuccessfulAuthenticationResult {
    private final boolean success = true;
    private final AssertionRequestWrapper request;
    private final AssertionResponse response;
    private final Collection<CredentialRegistration> registrations;

    @JsonSerialize(using = AuthDataSerializer.class)
    AuthenticatorData authData;

    private final String username;

    public SuccessfulAuthenticationResult(
            AssertionRequestWrapper request,
            AssertionResponse response,
            Collection<CredentialRegistration> registrations,
            String username) {
        this(
                request,
                response,
                registrations,
                response.getCredential().getResponse().getParsedAuthenticatorData(),
                username);
    }
}