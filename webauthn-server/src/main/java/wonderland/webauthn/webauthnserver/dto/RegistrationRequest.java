package wonderland.webauthn.webauthnserver.dto;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Optional;

@Value
@EqualsAndHashCode(callSuper = false)
public class RegistrationRequest {
  String username;
  Optional<String> credentialNickname;
  ByteArray requestId;
  PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions;
}
