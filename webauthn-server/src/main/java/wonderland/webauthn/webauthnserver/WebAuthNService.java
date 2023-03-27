package wonderland.webauthn.webauthnserver;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AttestationConveyancePreference;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wonderland.webauthn.webauthnserver.data.AssertionRequestWrapper;
import wonderland.webauthn.webauthnserver.data.CredentialRegistration;
import wonderland.webauthn.webauthnserver.data.RegistrationRequest;
import yubico.webauthn.attestation.YubicoJsonMetadataService;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WebAuthNService {

    private final InMemoryRegistrationStorage userStorage;
    private final RelyingParty rp;
    private final YubicoJsonMetadataService metadataService = new YubicoJsonMetadataService();
    private final Cache<ByteArray, AssertionRequestWrapper> assertRequestStorage = newCache();
    private final Cache<ByteArray, RegistrationRequest> registerRequestStorage = newCache();

    private static final RelyingPartyIdentity DEFAULT_RP_ID = RelyingPartyIdentity
            .builder().id("localhost").name("Yubico WebAuthn demo").build();
    private static final String DEFAULT_ORIGIN = "https://localhost:9568";

    private static <K, V> Cache<K, V> newCache() {
        return CacheBuilder
                .newBuilder()
                .maximumSize(100)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    public WebAuthNService(InMemoryRegistrationStorage userStorage) {
        this.userStorage = userStorage;
        //todo make bean
        rp = RelyingParty.builder()
                .identity(DEFAULT_RP_ID)
                .credentialRepository(this.userStorage)
                .origins(Set.of(DEFAULT_ORIGIN))
                .attestationConveyancePreference(Optional.of(AttestationConveyancePreference.DIRECT))
                .attestationTrustSource(metadataService)
                .allowOriginPort(false)
                .allowOriginSubdomain(false)
                .allowUntrustedAttestation(true)
                .validateSignatureCounter(true)
                .build();
    }

    public static ByteArray randomUUIDByteArray() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return new ByteArray(bb.array());
    }

    public RegistrationRequest startRegistration(@NonNull String username,
                                                 @NonNull String displayName,
                                                 Optional<String> credentialNickname,
                                                 ResidentKeyRequirement residentKeyRequirement) {
        log.trace("startRegistration username: {}, credentialNickname: {}", username, credentialNickname);

        final Set<CredentialRegistration> registrations = userStorage.getRegistrationsByUsername(username);
        final UserIdentity registrationUserId = registrations.stream()
                .findAny()
                .map(CredentialRegistration::getUserIdentity)
                .orElseGet(() -> UserIdentity.builder()
                        .name(username)
                        .displayName(displayName)
                        .id(randomUUIDByteArray())
                        .build()
                );

        var authenticatorSelectionCriteria = AuthenticatorSelectionCriteria.builder()
                .residentKey(residentKeyRequirement)
                .build();
        var startRegistrationOptions = StartRegistrationOptions.builder()
                .user(registrationUserId)
                .authenticatorSelection(authenticatorSelectionCriteria)
                .build();
        var registrationRequest = new RegistrationRequest(username, credentialNickname,
                randomUUIDByteArray(),
                rp.startRegistration(startRegistrationOptions));
        registerRequestStorage.put(registrationRequest.getRequestId(), registrationRequest);
        return registrationRequest;
    }

}
