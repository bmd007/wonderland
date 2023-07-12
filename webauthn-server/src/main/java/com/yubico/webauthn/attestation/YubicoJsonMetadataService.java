package com.yubico.webauthn.attestation;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yubico.internal.util.CertificateParser;
import com.yubico.internal.util.CollectionUtil;
import com.yubico.internal.util.OptionalUtil;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.attestation.matcher.ExtensionMatcher;
import com.yubico.webauthn.data.ByteArray;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("all")
@Slf4j
public final class YubicoJsonMetadataService implements AttestationTrustSource, MetadataService {

    private static final String SELECTORS = "selectors";
    private static final String SELECTOR_TYPE = "type";
    private static final String SELECTOR_PARAMETERS = "parameters";

    private static final Map<String, DeviceMatcher> DEFAULT_DEVICE_MATCHERS =
            Map.of(ExtensionMatcher.SELECTOR_TYPE, new ExtensionMatcher());

    private final Collection<MetadataObject> metadataObjects;
    private final Map<String, DeviceMatcher> matchers;
    private final Set<X509Certificate> trustRootCertificates;

    private YubicoJsonMetadataService(
            @NonNull Collection<MetadataObject> metadataObjects,
            @NonNull Map<String, DeviceMatcher> matchers) {
        this.trustRootCertificates =
                Collections.unmodifiableSet(
                        metadataObjects.stream()
                                .flatMap(metadataObject -> metadataObject.getTrustedCertificates().stream())
                                .map(
                                        pemEncodedCert -> {
                                            try {
                                                return CertificateParser.parsePem(pemEncodedCert);
                                            } catch (CertificateException e) {
                                                log.error("Failed to parse trusted certificate", e);
                                                return null;
                                            }
                                        })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet()));
        this.metadataObjects = metadataObjects;
        this.matchers = CollectionUtil.immutableMap(matchers);
    }

    public YubicoJsonMetadataService() {
        this(
                Stream.of(MetadataObject.readDefault(), MetadataObject.readPreview())
                        .collect(Collectors.toList()),
                DEFAULT_DEVICE_MATCHERS);
    }

    @Override
    public Set<Object> findEntries(@NonNull RegistrationResult registrationResult) {
        return registrationResult
                .getAttestationTrustPath()
                .map(
                        certs -> {
                            X509Certificate attestationCertificate = certs.get(0);
                            return metadataObjects.stream()
                                    .map(
                                            metadata -> {
                                                Map<String, String> vendorProperties;
                                                Map<String, String> deviceProperties = null;
                                                String identifier;

                                                identifier = metadata.getIdentifier();
                                                vendorProperties =
                                                        Maps.filterValues(metadata.getVendorInfo(), Objects::nonNull);
                                                for (JsonNode device : metadata.getDevices()) {
                                                    if (deviceMatches(device.get(SELECTORS), attestationCertificate)) {
                                                        ImmutableMap.Builder<String, String> devicePropertiesBuilder =
                                                                ImmutableMap.builder();
                                                        for (Map.Entry<String, JsonNode> deviceEntry :
                                                                Lists.newArrayList(device.fields())) {
                                                            JsonNode value = deviceEntry.getValue();
                                                            if (value.isTextual()) {
                                                                devicePropertiesBuilder.put(deviceEntry.getKey(), value.asText());
                                                            }
                                                        }
                                                        deviceProperties = devicePropertiesBuilder.build();
                                                        break;
                                                    }
                                                }

                                                return Optional.ofNullable(deviceProperties)
                                                        .map(
                                                                deviceProps ->
                                                                        Attestation.builder()
                                                                                .metadataIdentifier(Optional.ofNullable(identifier))
                                                                                .vendorProperties(Optional.of(vendorProperties))
                                                                                .deviceProperties(deviceProps)
                                                                                .build());
                                            })
                                    .flatMap(OptionalUtil::stream)
                                    .map(Object.class::cast)
                                    .collect(Collectors.toSet());
                        })
                .orElseGet(Collections::emptySet);
    }

    private boolean deviceMatches(
            JsonNode selectors, @NonNull X509Certificate attestationCertificate) {
        if (selectors == null || selectors.isNull()) {
            return true;
        } else {
            for (JsonNode selector : selectors) {
                DeviceMatcher matcher = matchers.get(selector.get(SELECTOR_TYPE).asText());
                if (matcher != null
                        && matcher.matches(attestationCertificate, selector.get(SELECTOR_PARAMETERS))) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public TrustRootsResult findTrustRoots(
            List<X509Certificate> attestationCertificateChain, Optional<ByteArray> aaguid) {
        return TrustRootsResult.builder()
                .trustRoots(trustRootCertificates)
                .enableRevocationChecking(false)
                .build();
    }
}
