import com.github.tomakehurst.wiremock.client.WireMock;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.nimbusds.jose.JWSAlgorithm.RS256;
import static com.nimbusds.jose.jwk.gen.RSAKeyGenerator.MIN_KEY_SIZE_BITS;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@ActiveProfiles("local-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestConfig.class})
@AutoConfigureWireMock(port = 0)
@DirtiesContext
@AutoConfigureWebTestClient
class ApplicationTest {
    private static final UUID CUSTOMER_ID = UUID.fromString("e9a7b89d-1a2b-4f44-87ac-698c89bc7e11");
    private static final RSAKeyGenerator RSA_KEY_GENERATOR = new RSAKeyGenerator(MIN_KEY_SIZE_BITS);
    static {
        RSA_KEY_GENERATOR.keyUse(KeyUse.SIGNATURE);
        RSA_KEY_GENERATOR.keyIDFromThumbprint(true);
        RSA_KEY_GENERATOR.algorithm(RS256);
        RSA_KEY_GENERATOR.expirationTime(Date.from(Instant.now().plusSeconds(3600)));
    }
    private String serializedSignedJwt(JWTClaimsSet claimsSet, RSAKey rsaKeyPair) {
        try {
            final JWSSigner signer = new RSASSASigner(rsaKeyPair.toPrivateKey());
            final JWSHeader jwsHeader = new JWSHeader.Builder(RS256)
                    .keyID(rsaKeyPair.getKeyID())
                    .type(JOSEObjectType.JWT)
                    .build();
            final SignedJWT signedJWT = new SignedJWT(jwsHeader, claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private WebTestClient webTestClient;
    private RSAKey rsaKeyPair;

    @BeforeEach
    public void beforeEach() throws JOSEException {
        rsaKeyPair = RSA_KEY_GENERATOR.generate();
        stubFor(WireMock.get(urlPathEqualTo("/.well-known/jwks.json"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("content-type", TEXT_HTML_VALUE)
                        .withBody(new JWKSet(rsaKeyPair.toPublicJWK()).toString())));
    }

    @Test
    void getCustomerId() {
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .issuer("https://www.bmd007.io")
                .subject(CUSTOMER_ID.toString())
                .expirationTime(new Date(Instant.now().plusSeconds(30).toEpochMilli()))
                .build();
        String serializedSignedJwt = serializedSignedJwt(jwtClaimsSet, rsaKeyPair);
        webTestClient.get()
                .uri("/v1/customers")
                .header("custom_authorization", "Bearer %s".formatted(serializedSignedJwt))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("authenticated %s".formatted(CUSTOMER_ID.toString()));
    }
}
