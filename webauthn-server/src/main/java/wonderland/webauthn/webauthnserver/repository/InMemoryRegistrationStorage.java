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
public class InMemoryRegistrationStorage implements CredentialRepository {

    private final Map<String, Set<CredentialRegistration>> storage = new HashMap<>();

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
                        .flatMap(Collection::stream)
                        .filter(credReg -> credentialId.equals(credReg.getCredential().getCredentialId()))
                        .findAny();

        log.info(
                "lookup credential ID: {}, user handle: {}; result: {}",
                credentialId,
                userHandle,
                registrationMaybe);
        return registrationMaybe.map(
                registration ->
                        RegisteredCredential.builder()
                                .credentialId(registration.getCredential().getCredentialId())
                                .userHandle(registration.getUserIdentity().getId())
                                .publicKeyCose(registration.getCredential().getPublicKeyCose())
                                .signatureCount(registration.getCredential().getSignatureCount())
                                .build());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return Set.copyOf(
                storage.values().stream()
                        .flatMap(Collection::stream)
                        .filter(reg -> reg.getCredential().getCredentialId().equals(credentialId))
                        .map(reg ->
                                RegisteredCredential.builder()
                                        .credentialId(reg.getCredential().getCredentialId())
                                        .userHandle(reg.getUserIdentity().getId())
                                        .publicKeyCose(reg.getCredential().getPublicKeyCose())
                                        .signatureCount(reg.getCredential().getSignatureCount())
                                        .build())
                        .collect(Collectors.toSet())
        );
    }

    public boolean addRegistrationByUsername(String username, CredentialRegistration reg) {
        return storage.get(username).add(reg);
    }

    public Set<CredentialRegistration> getRegistrationsByUsername(String username) {
        return storage.get(username);
    }

    public List<CredentialRegistration> getRegistrationsByUserHandle(ByteArray userHandle) {
        return storage.values().stream()
                .flatMap(Collection::stream)
                .filter(credentialRegistration -> userHandle.equals(credentialRegistration.getUserIdentity().getId()))
                .collect(Collectors.toList());
    }

    public void updateSignatureCount(AssertionResult result) {
        CredentialRegistration registration =
                getRegistrationByUsernameAndCredentialId(
                        result.getUsername(), result.getCredential().getCredentialId())
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                String.format(
                                                        "Credential \"%s\" is not registered to user \"%s\"",
                                                        result.getCredential().getCredentialId(), result.getUsername())));

        var credentialRegistrations = storage.get(result.getUsername());
        credentialRegistrations.remove(registration);
        var registeredCredential = registration.getCredential()
                .toBuilder()
                .signatureCount(result.getSignatureCount())
                .build();
        credentialRegistrations.add(registration.withCredential(registeredCredential));
    }

    public Optional<CredentialRegistration> getRegistrationByUsernameAndCredentialId(String username, ByteArray id) {
        return storage.get(username).stream()
                .filter(credReg -> id.equals(credReg.getCredential().getCredentialId()))
                .findFirst();
    }

    public boolean removeRegistrationByUsername(String username, CredentialRegistration credentialRegistration) {
        return storage.get(username).remove(credentialRegistration);
    }

    public boolean removeAllRegistrations(String username) {
        storage.remove(username);
        return true;
    }

    public boolean userExists(String username) {
        return !getRegistrationsByUsername(username).isEmpty();
    }
}
