package wonderland.webauthn.webauthnserver.dto;

import com.yubico.internal.util.CertificateParser;
import com.yubico.webauthn.data.ByteArray;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Slf4j
@Value
public class AttestationCertInfo {
    final ByteArray der;
    final String text;

    public AttestationCertInfo(ByteArray certDer) {
        der = certDer;
        X509Certificate cert = null;
        try {
            cert = CertificateParser.parseDer(certDer.getBytes());
        } catch (CertificateException e) {
            log.error("Failed to parse attestation certificate");
        }
        if (cert == null) {
            text = null;
        } else {
            text = cert.toString();
        }
    }
}