package com.yubico.webauthn.attestation;

import com.yubico.webauthn.RegistrationResult;
import lombok.NonNull;

import java.util.Set;

public interface MetadataService extends AttestationTrustSource {
    Set<Object> findEntries(@NonNull RegistrationResult registrationResult);
}
