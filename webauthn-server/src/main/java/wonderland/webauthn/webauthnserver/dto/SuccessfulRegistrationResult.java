package wonderland.webauthn.webauthnserver.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yubico.webauthn.data.AuthenticatorData;
import com.yubico.webauthn.data.ByteArray;
import demo.webauthn.WebAuthnServer;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import wonderland.webauthn.webauthnserver.domain.CredentialRegistration;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Value
public class SuccessfulRegistrationResult {
    final boolean success = true;
    RegistrationRequest request;
    RegistrationResponse response;
    CredentialRegistration registration;
    boolean attestationTrusted;
    Optional<WebAuthnServer.AttestationCertInfo> attestationCert;

    @JsonSerialize(using = AuthDataSerializer.class)
    AuthenticatorData authData;

    String username;

    public SuccessfulRegistrationResult(
            RegistrationRequest request,
            RegistrationResponse response,
            CredentialRegistration registration,
            boolean attestationTrusted) {
        this.request = request;
        this.response = response;
        this.registration = registration;
        this.attestationTrusted = attestationTrusted;
        attestationCert =
                Optional.ofNullable(
                                response
                                        .credential()
                                        .getResponse()
                                        .getAttestation()
                                        .getAttestationStatement()
                                        .get("x5c"))
                        .map(certs -> certs.get(0))
                        .flatMap(
                                (JsonNode certDer) -> {
                                    try {
                                        return Optional.of(new ByteArray(certDer.binaryValue()));
                                    } catch (IOException e) {
                                        log.error("Failed to get binary value from x5c element: {}", certDer, e);
                                        return Optional.empty();
                                    }
                                })
                        .map(WebAuthnServer.AttestationCertInfo::new);
        this.authData = response.credential().getResponse().getParsedAuthenticatorData();
        this.username = request.getUsername();
    }
}
