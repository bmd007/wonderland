package wonderland.webauthn.webauthnserver;

import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wonderland.webauthn.webauthnserver.data.CredentialRegistration;
import wonderland.webauthn.webauthnserver.data.RegistrationRequest;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class WebAuthNService {

    private final InMemoryRegistrationStorage userStorage;

    public WebAuthNService(InMemoryRegistrationStorage userStorage) {
        this.userStorage = userStorage;
    }

    public static ByteArray randomUUIDByteArray() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return new ByteArray(bb.array());
    }

    public RegistrationRequest startRegistration(
            @NonNull String username,
            @NonNull String displayName,
            Optional<String> credentialNickname,
            ResidentKeyRequirement residentKeyRequirement)
            throws ExecutionException {
        log.trace("startRegistration username: {}, credentialNickname: {}", username, credentialNickname);

        final Set<CredentialRegistration> registrations = userStorage.getRegistrationsByUsername(username);
        final UserIdentity registrationUserId =
                registrations.stream()
                        .findAny()
                        .map(CredentialRegistration::getUserIdentity)
                        .orElseGet(() -> UserIdentity.builder()
                                                .name(username)
                                                .displayName(displayName)
                                                .id(randomUUIDByteArray())
                                                .build());

            RegistrationRequest request =
                    new RegistrationRequest(
                            username,
                            credentialNickname,
                            generateRandom(32),
                            rp.startRegistration(
                                    StartRegistrationOptions.builder()
                                            .user(registrationUserId)
                                            .authenticatorSelection(
                                                    AuthenticatorSelectionCriteria.builder()
                                                            .residentKey(residentKeyRequirement)
                                                            .build())
                                            .build()),
                            Optional.of(sessions.createSession(registrationUserId.getId())));
            registerRequestStorage.put(request.getRequestId(), request);
            return Either.right(request);
    }

}
