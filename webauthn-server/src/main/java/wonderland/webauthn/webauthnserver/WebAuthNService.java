package wonderland.webauthn.webauthnserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AttestationConveyancePreference;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import wonderland.webauthn.webauthnserver.domain.CredentialRegistration;
import wonderland.webauthn.webauthnserver.dto.AssertionRequestWrapper;
import wonderland.webauthn.webauthnserver.dto.AssertionResponse;
import wonderland.webauthn.webauthnserver.dto.RegistrationRequest;
import wonderland.webauthn.webauthnserver.dto.RegistrationResponse;
import wonderland.webauthn.webauthnserver.dto.SuccessfulAuthenticationResult;
import wonderland.webauthn.webauthnserver.dto.SuccessfulRegistrationResult;
import yubico.webauthn.attestation.Attestation;
import yubico.webauthn.attestation.YubicoJsonMetadataService;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
    private final ObjectMapper objectMapper;

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

    public WebAuthNService(InMemoryRegistrationStorage userStorage, ObjectMapper objectMapper) {
        this.userStorage = userStorage;
        this.objectMapper = objectMapper;
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


    public SuccessfulRegistrationResult finishRegistration(RegistrationResponse registrationResponse) {
        var registrationRequest =
                Optional.ofNullable(registerRequestStorage.getIfPresent(registrationResponse.requestId()))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "registration request not found"));
        registerRequestStorage.invalidate(registrationRequest.getRequestId());

        try {
            var registrationResult = rp.finishRegistration(
                    FinishRegistrationOptions.builder()
                            .request(registrationRequest.getPublicKeyCredentialCreationOptions())
                            .response(registrationResponse.credential())
                            .build()
            );

            var credentialsRegistration = addRegistration(
                    registrationRequest.getPublicKeyCredentialCreationOptions().getUser(),
                    registrationRequest.getCredentialNickname(),
                    registrationResult);

            return new SuccessfulRegistrationResult(
                    registrationRequest,
                    registrationResponse,
                    credentialsRegistration,
                    registrationResult.isAttestationTrusted());
        } catch (RegistrationFailedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    private CredentialRegistration addRegistration(
            UserIdentity userIdentity,
            Optional<String> nickname,
            RegistrationResult result) {
        var registeredCredential = RegisteredCredential.builder()
                .credentialId(result.getKeyId().getId())
                .userHandle(userIdentity.getId())
                .publicKeyCose(result.getPublicKeyCose())
                .signatureCount(result.getSignatureCount())
                .build();
        SortedSet<AuthenticatorTransport> transports = result.getKeyId().getTransports().orElseGet(TreeSet::new);
        Optional<Attestation> attestationMetadata = result.getAttestationTrustPath()
                .flatMap(x5c -> x5c.stream().findFirst())
                .flatMap(metadataService::findMetadata);
        return addRegistration(
                userIdentity,
                nickname,
                registeredCredential,
                transports,
                attestationMetadata);
    }

    private CredentialRegistration addRegistration(
            UserIdentity userIdentity,
            Optional<String> nickname,
            RegisteredCredential credential,
            SortedSet<AuthenticatorTransport> transports,
            Optional<Attestation> attestationMetadata) {
        CredentialRegistration reg = CredentialRegistration.builder()
                .userIdentity(userIdentity)
                .credentialNickname(nickname)
                .registrationTime(Instant.now())
                .credential(credential)
                .transports(transports)
                .attestationMetadata(attestationMetadata)
                .build();
        log.debug("Adding registration: user: {}, nickname: {}, credential: {}",
                userIdentity,
                nickname,
                credential);
        userStorage.addRegistrationByUsername(userIdentity.getName(), reg);
        return reg;
    }

    public AssertionRequestWrapper startAuthentication(String username) {
        log.trace("startAuthentication username: {}", username);

        if (!userStorage.userExists(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not registered");
        } else {
            var startAssertionOptions = StartAssertionOptions.builder().username(username).build();
            var assertionRequestWrapper = new AssertionRequestWrapper(
                    randomUUIDByteArray(), rp.startAssertion(startAssertionOptions));

            assertRequestStorage.put(assertionRequestWrapper.getRequestId(), assertionRequestWrapper);

            return assertionRequestWrapper;
        }
    }


    public SuccessfulAuthenticationResult finishAuthentication(AssertionResponse assertionResponse) {
        AssertionRequestWrapper request =
                Optional.ofNullable(assertRequestStorage.getIfPresent(assertionResponse.getRequestId()))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "assertion request not found"));
        assertRequestStorage.invalidate(assertionResponse.getRequestId());
        try {
            var finishAssertionOptions = FinishAssertionOptions.builder()
                    .request(request.getRequest())
                    .response(assertionResponse.getCredential())
                    .build();
            var assertionResult = rp.finishAssertion(finishAssertionOptions);
            if (assertionResult.isSuccess()) {
                updateSignatureCountForUser(assertionResponse, assertionResult);
                var registrationsByUsername = userStorage.getRegistrationsByUsername(assertionResult.getUsername());
                return new SuccessfulAuthenticationResult(
                        request,
                        assertionResponse,
                        registrationsByUsername,
                        assertionResult.getUsername());
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Assertion failed.");
            }
        } catch (Exception e) {
            log.error("Assertion failed", e);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Assertion failed unexpectedly; this is likely a bug.");
        }
    }

    private void updateSignatureCountForUser(AssertionResponse assertionResponse, AssertionResult assertionResult) {
        try {
            userStorage.updateSignatureCount(assertionResult);
        } catch (Exception e) {
            log.error(
                    "Failed to update signature count for user \"{}\", credential \"{}\"",
                    assertionResult.getUsername(),
                    assertionResponse.getCredential().getId(),
                    e);
        }
    }

}
