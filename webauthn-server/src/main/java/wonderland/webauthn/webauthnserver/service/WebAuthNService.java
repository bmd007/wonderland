
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AssertionExtensionInputs;
import com.yubico.webauthn.data.AttestationConveyancePreference;
import com.yubico.webauthn.data.AuthenticatorAttachment;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import com.yubico.webauthn.data.RegistrationExtensionInputs;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import com.yubico.webauthn.extension.appid.AppId;
import com.yubico.webauthn.extension.appid.InvalidAppIdException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import se.nordnet.authentication.webauthn.domain.CredentialRegistration;
import se.nordnet.authentication.webauthn.dto.AssertionRequestWrapper;
import se.nordnet.authentication.webauthn.dto.AssertionResponse;
import se.nordnet.authentication.webauthn.dto.RegistrationRequest;
import se.nordnet.authentication.webauthn.dto.RegistrationResponse;
import se.nordnet.authentication.webauthn.dto.SuccessfulAuthenticationResult;
import se.nordnet.authentication.webauthn.dto.SuccessfulRegistrationResult;
import se.nordnet.authentication.webauthn.repository.InMemoryRegistrationStorage;
import yubico.webauthn.attestation.Attestation;
import yubico.webauthn.attestation.YubicoJsonMetadataService;

import javax.validation.constraints.NotBlank;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
public class WebAuthNService {

    public static final RelyingPartyIdentity SWEDISH_RELYING_PARTY_IDENTITY = RelyingPartyIdentity.builder().id("nordnet.se").name("Nordnet WebAuthn se").build();
    public static final RelyingPartyIdentity DANISH_RELYING_PARTY_IDENTITY = RelyingPartyIdentity.builder().id("nordnet.dk").name("Nordnet WebAuthn dk").build();
    private final InMemoryRegistrationStorage userStorage;
    private final RelyingParty swedishRelyingParty;
    private final RelyingParty danishRelyingParty;
    private final YubicoJsonMetadataService metadataService = new YubicoJsonMetadataService();
    private final Map<ByteArray, AssertionRequestWrapper> assertRequestStorage = new HashMap<>();
    private final Map<ByteArray, RegistrationRequest> registerRequestStorage = new HashMap<>();

    private static final String LOCAL_DOMAIN = "https://localhost.localdomain";
    private static final String WEBAPP_NEXT_PROD_DK = "https://nordnet.dk";
    private static final String WEBAPP_NEXT_PROD_NO = "https://nordnet.no";
    private static final String WEBAPP_NEXT_PROD_SE = "https://nordnet.se";
    private static final String WEBAPP_NEXT_PROD_FI = "https://nordnet.fi";
    private static final String WEBAPP_NEXT_APPID_ORIGIN_PATTERN = "https:\\/\\/(?:[^\\/]*\\.)?(?:nordnet\\.(?:dk|no|fi)|test\\.nordnet\\.(?:dk|no|fi)|localhost\\.localdomain(?::808[01])?|local\\.next\\.test\\.nordnet\\.dk:808[01])".trim();
    private static final Pattern WEBAPP_NEXT_APPID_PATTERN = Pattern.compile(WEBAPP_NEXT_APPID_ORIGIN_PATTERN);

    public WebAuthNService(InMemoryRegistrationStorage userStorage) throws InvalidAppIdException {
        this.userStorage = userStorage;
        swedishRelyingParty = RelyingParty.builder()
                .identity(SWEDISH_RELYING_PARTY_IDENTITY)
                .credentialRepository(this.userStorage)
                .origins(Set.of(LOCAL_DOMAIN, WEBAPP_NEXT_PROD_SE))
                .allowOriginPort(true)
                .allowOriginSubdomain(true)
                .attestationConveyancePreference(AttestationConveyancePreference.DIRECT)
                .attestationTrustSource(metadataService)
                .allowUntrustedAttestation(true)
//                .validateSignatureCounter(true)
                .appId(new AppId(WEBAPP_NEXT_PROD_SE))
                .build();
        danishRelyingParty = RelyingParty.builder()
                .identity(DANISH_RELYING_PARTY_IDENTITY)
                .credentialRepository(this.userStorage)
                .origins(Set.of(LOCAL_DOMAIN, WEBAPP_NEXT_PROD_DK))
                .allowOriginPort(true)
                .allowOriginSubdomain(true)
                .attestationConveyancePreference(AttestationConveyancePreference.DIRECT)
                .attestationTrustSource(metadataService)
                .allowUntrustedAttestation(true)
//                .validateSignatureCounter(true)
                .appId(new AppId(WEBAPP_NEXT_PROD_DK))
                .build();
    }

    public AppId getAppid(@NotBlank String origin) {
        try {
            if (WEBAPP_NEXT_APPID_PATTERN.matcher(origin).matches()) {
                URL url = URI.create(origin).toURL();
                return new AppId(url.toString());
            }
        } catch (MalformedURLException | InvalidAppIdException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "bad origin %s".formatted(origin), e);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "bad origin %s".formatted(origin));
    }

    public static ByteArray randomUUIDByteArray() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return new ByteArray(bb.array());
    }

    public RegistrationRequest startRegistration(@NotBlank String username,
                                                 @NotBlank String displayName,
                                                 Optional<String> credentialNickname,
                                                 String countryCode,
                                                 AuthenticatorAttachment authenticatorAttachment) {
        final UserIdentity userIdentity =
                Optional.ofNullable(userStorage.getRegistrationsByUsername(username))
                        .stream()
                        .flatMap(Collection::stream)
                        .findAny()
                        .map(CredentialRegistration::getUserIdentity)
                        .orElseGet(() -> UserIdentity.builder()
                                .name(username)
                                .displayName(displayName)
                                .id(randomUUIDByteArray())
                                .build()
                        );
        var authenticatorSelectionCriteria = AuthenticatorSelectionCriteria.builder()
                .residentKey(ResidentKeyRequirement.REQUIRED)
                .authenticatorAttachment(authenticatorAttachment)
                .userVerification(UserVerificationRequirement.PREFERRED)
                .build();
        var registrationExtensionInputs = RegistrationExtensionInputs.builder()
                .credProps()
                .uvm()
                .build();
        var startRegistrationOptions = StartRegistrationOptions.builder()
                .user(userIdentity)
                .authenticatorSelection(authenticatorSelectionCriteria)
                .timeout(999_999_999L)
                .extensions(registrationExtensionInputs)
                .build();
        var publicKeyCredentialCreationOptions = getRelyingParty(countryCode).startRegistration(startRegistrationOptions);
        var registrationRequest = new RegistrationRequest(username,
                credentialNickname,
                randomUUIDByteArray(),
                publicKeyCredentialCreationOptions);
        registerRequestStorage.put(registrationRequest.getRequestId(), registrationRequest);
        return registrationRequest;
    }

    private RelyingParty getRelyingParty(String countryCode) {
        return switch (countryCode.toLowerCase()) {
            case "dk" -> danishRelyingParty;
            default -> swedishRelyingParty;
        };
    }

    public SuccessfulRegistrationResult finishRegistration(RegistrationResponse registrationResponse, String countryCode) {
        var registrationRequest =
                Optional.ofNullable(registerRequestStorage.get(registrationResponse.requestId()))
                        .orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "registration request not found"));
        registerRequestStorage.remove(registrationRequest.getRequestId());
        try {
            var registrationResult = getRelyingParty(countryCode).finishRegistration(
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
        var credentialRegistration = CredentialRegistration.builder()
                .userIdentity(userIdentity)
                .credentialNickname(nickname)
                .registrationTime(Instant.now())
                .credential(credential)
                .transports(transports)
                .attestationMetadata(attestationMetadata)
                .build();
        log.info("Adding registration: user: {}, nickname: {}, credential: {}",
                userIdentity,
                nickname,
                credential);
        userStorage.addRegistrationByUsername(userIdentity.getName(), credentialRegistration);
        return credentialRegistration;
    }

    public AssertionRequestWrapper startAuthentication(String username, String countryCode) {
        if (!userStorage.userExists(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not registered");
        } else {
            var assertionExtensionInputs = AssertionExtensionInputs.builder()
                    .uvm()
                    .build();
            var startAssertionOptions = StartAssertionOptions.builder()
                    .userVerification(UserVerificationRequirement.REQUIRED)//NOPMD TODO try it on a mac with finger print sensor and see if still asks for password
                    .extensions(assertionExtensionInputs)
                    .username(username)
                    .timeout(999_999_999L)
                    .build();
            AssertionRequest assertionRequest = getRelyingParty(countryCode).startAssertion(startAssertionOptions);
            var assertionRequestWrapper = new AssertionRequestWrapper(randomUUIDByteArray(), assertionRequest);
            assertRequestStorage.put(assertionRequestWrapper.getRequestId(), assertionRequestWrapper);
            return assertionRequestWrapper;
        }
    }

    public AssertionRequestWrapper startAuthentication(String countryCode) {
        var assertionExtensionInputs = AssertionExtensionInputs.builder()
                .uvm()
                .build();
        var startAssertionOptions = StartAssertionOptions.builder()
                .userVerification(UserVerificationRequirement.PREFERRED)//NOPMD todo Does DISCOURAGED here have security issues?
                //NOPMD todo also, discouraged worked once? or never worked? the yubikey gets confused about the key owner
                .extensions(assertionExtensionInputs)
                .timeout(999_999_999L)
                .build();
        AssertionRequest assertionRequest = getRelyingParty(countryCode).startAssertion(startAssertionOptions);
        //remove that package and this commented code, once the library has the bug fixed
        PublicKeyCredentialRequestOptions publicKeyOptionsWithAllowCredentials = assertionRequest.getPublicKeyCredentialRequestOptions()
                .toBuilder()
                .allowCredentials(List.of())
                .build();
        AssertionRequest improvedAssertionRequest = assertionRequest.toBuilder()
                .publicKeyCredentialRequestOptions(publicKeyOptionsWithAllowCredentials)
                .build();
        var assertionRequestWrapper = new AssertionRequestWrapper(randomUUIDByteArray(), improvedAssertionRequest);
        assertRequestStorage.put(assertionRequestWrapper.getRequestId(), assertionRequestWrapper);
        return assertionRequestWrapper;
    }

    public AssertionRequestWrapper startAuthentication(ByteArray userHandle, String countryCode) {
        Collection<CredentialRegistration> registrationsByUserHandle = userStorage.getRegistrationsByUserHandle(userHandle);
        if (registrationsByUserHandle.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "no registration found for handle " + userHandle);
        }
        var assertionExtensionInputs = AssertionExtensionInputs.builder()
                .uvm()
                .build();
        var startAssertionOptions = StartAssertionOptions.builder()
                .userVerification(UserVerificationRequirement.PREFERRED)//NOPMD todo Does DISCOURAGED here have security issues?
                //NOPMD todo also, discouraged worked once? or never worked? the yubikey gets confused about the key owner
                .extensions(assertionExtensionInputs)
                .userHandle(userHandle)
                .timeout(999_999_999L)
                .build();
        AssertionRequest assertionRequest = getRelyingParty(countryCode).startAssertion(startAssertionOptions);
        var assertionRequestWrapper = new AssertionRequestWrapper(randomUUIDByteArray(), assertionRequest);
        assertRequestStorage.put(assertionRequestWrapper.getRequestId(), assertionRequestWrapper);
        return assertionRequestWrapper;
    }

    public SuccessfulAuthenticationResult finishAuthentication(AssertionResponse assertionResponse, String countryCode) {
        AssertionRequestWrapper request =
                Optional.ofNullable(assertRequestStorage.get(assertionResponse.requestId()))
                        .orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "assertion request not found"));
        assertRequestStorage.remove(assertionResponse.requestId());
        try {
            var finishAssertionOptions = FinishAssertionOptions.builder()
                    .request(request.getRequest())
                    .response(assertionResponse.credential())
                    .build();
            var assertionResult = getRelyingParty(countryCode).finishAssertion(finishAssertionOptions);
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
        } catch (AssertionFailedException e) {
            log.error("Assertion failed", e);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Assertion failed unexpectedly; this is likely a bug.", e);
        }
    }

    private void updateSignatureCountForUser(AssertionResponse assertionResponse, AssertionResult assertionResult) {
        log.info(
                "update signature count for user {}, credential {}",
                assertionResult.getUsername(),
                assertionResponse.credential().getId());
        userStorage.updateSignatureCount(assertionResult);
    }

    public void deleteAll() {
        userStorage.removeAll();
        assertRequestStorage.clear();
        registerRequestStorage.clear();
    }
}
