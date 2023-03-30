package wonderland.webauthn.webauthnserver.repository;

import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import wonderland.webauthn.webauthnserver.domain.CredentialRegistration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class SmallInMemoryRegistrationStorage implements CredentialRepository {

    private final Map<String, CredentialRegistration> storage = new HashMap<>();

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return Optional.ofNullable(getRegistrationsByUsername(username))
                .stream()
                .flatMap(Collection::stream)
                .map(registration ->
                        PublicKeyCredentialDescriptor.builder()
                                .id(registration.getCredential().getCredentialId())
                                .transports(registration.getTransports())
                                .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return getRegistrationsByUserHandle(userHandle).stream()
                .findAny()
                .map(CredentialRegistration::getUsername);
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return getRegistrationsByUsername(username).stream()
                .findAny()
                .map(reg -> reg.getUserIdentity().getId());
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        Optional<CredentialRegistration> registrationMaybe =
                storage.values().stream()
                        .filter(credReg -> credentialId.equals(credReg.getCredential().getCredentialId()))
                        .findAny();

        log.info("lookup credential ID: {}, user handle: {}; result: {}", credentialId, userHandle, registrationMaybe);
        return registrationMaybe.map(registration ->
                        RegisteredCredential.builder()
                                .credentialId(registration.getCredential().getCredentialId())
                                .userHandle(registration.getUserIdentity().getId())
                                .publicKeyCose(registration.getCredential().getPublicKeyCose())
                                .signatureCount(registration.getCredential().getSignatureCount())
                                .build());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return storage.values().stream()
                        .filter(reg -> reg.getCredential().getCredentialId().equals(credentialId))
                        .map(reg ->
                                RegisteredCredential.builder()
                                        .credentialId(reg.getCredential().getCredentialId())
                                        .userHandle(reg.getUserIdentity().getId())
                                        .publicKeyCose(reg.getCredential().getPublicKeyCose())
                                        .signatureCount(reg.getCredential().getSignatureCount())
                                        .build())
                        .collect(Collectors.toSet());
    }

    public boolean addRegistrationByUsername(String username, CredentialRegistration reg) {
        return storage.put(username, reg) != null;
    }

    public Set<CredentialRegistration> getRegistrationsByUsername(String username) {
        return Optional.ofNullable(storage.get(username))
                .stream()
                .collect(Collectors.toSet());
    }

    public List<CredentialRegistration> getRegistrationsByUserHandle(ByteArray userHandle) {
        return storage.values().stream()
                .filter(credentialRegistration -> userHandle.equals(credentialRegistration.getUserIdentity().getId()))
                .collect(Collectors.toList());
    }

    public void updateSignatureCount(AssertionResult result) {
        var credentialRegistration =
                getRegistrationByUsernameAndCredentialId(
                        result.getUsername(), result.getCredential().getCredentialId())
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                String.format(
                                                        "Credential \"%s\" is not registered to user \"%s\"",
                                                        result.getCredential().getCredentialId(), result.getUsername())));

        var registeredCredential = credentialRegistration.getCredential()
                .toBuilder()
                .signatureCount(result.getSignatureCount())
                .build();
        var updatedCredentialRegistration = credentialRegistration.withCredential(registeredCredential);
        storage.put(result.getUsername(), updatedCredentialRegistration);
    }

    public Optional<CredentialRegistration> getRegistrationByUsernameAndCredentialId(String username, ByteArray id) {
        return Optional.ofNullable(storage.get(username))
                .filter(credReg -> id.equals(credReg.getCredential().getCredentialId()));
    }

    public boolean removeRegistrationByUsername(String username, CredentialRegistration credentialRegistration) {
        return storage.remove(username).getCredential().getCredentialId() == credentialRegistration.getCredential().getCredentialId();
    }

    public boolean removeAllRegistrations(String username) {
        storage.remove(username);
        return true;
    }

    public boolean userExists(String username) {
        return Optional.ofNullable(getRegistrationsByUsername(username)).isPresent();
    }
}
