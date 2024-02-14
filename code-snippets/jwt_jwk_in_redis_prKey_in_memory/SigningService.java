import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.text.ParseException;

import static com.nimbusds.jose.JWSAlgorithm.RS256;

@Slf4j
@Service
public class SigningService {

    private static final RSAKeyGenerator KEYGEN = new RSAKeyGenerator(MIN_KEY_SIZE_BITS);

    static {
        KEYGEN.keyUse(KeyUse.SIGNATURE);
        KEYGEN.keyIDFromThumbprint(true);
        KEYGEN.algorithm(RS256);
    }

    private RSAUtils() {

    }

    public static RSAKey newRSAKey() {
        try {
            return KEYGEN.generate();
        } catch (JOSEException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed generating a new RSA key", e);
        }
    }

    public static final String JWK_KEY_PREFIX = "JWK_";
    private final ReactiveStringRedisTemplate stringRedisTemplate;
    private final TimeToLiveProperties timeToLiveProperties;
    private static final Integer PRIVATE_KEY_LOCAL_INDEX = 0;
    private final Cache<Integer, RSAKey> localRsaKeyStore;

    public SigningService(ReactiveStringRedisTemplate stringRedisTemplate, TimeToLiveProperties timeToLiveProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.timeToLiveProperties = timeToLiveProperties;
        localRsaKeyStore = CacheBuilder.newBuilder()
                .removalListener(notification -> log.info("Expired public key removed from local memory {}", notification))
                .concurrencyLevel(20) // todo is 20 enough?
                .expireAfterWrite(timeToLiveProperties.getPrivateKey())
                .build();
    }

    private Mono<RSAKey> createNewKey() {
        return Mono.just(RSAUtils.newRSAKey())
                .delayUntil(this::saveAsPublicJWK)
                .delayUntil(newRSAKey -> Mono.fromRunnable(() -> localRsaKeyStore.put(PRIVATE_KEY_LOCAL_INDEX, newRSAKey)))
                .doOnError(throwable -> log.error("Failed to create key pairs and save them in local storage and/or redis", throwable))
                .onErrorMap(exception -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create key pairs", exception));
    }

    private Mono<RSAKey> getRSAKey() {
        return Mono.justOrEmpty(localRsaKeyStore.getIfPresent(PRIVATE_KEY_LOCAL_INDEX))
                .switchIfEmpty(createNewKey())
                .doOnError(throwable -> log.error("Failed fetching keys from local memory or rotating them", throwable));
    }

    private Mono<Boolean> saveAsPublicJWK(RSAKey rsaKey) {
        String rsaPulicKeyJsonString = rsaKey.toPublicJWK().toJSONString();
        String redisQueryFriendlyKeyId = String.format(JWK_KEY_PREFIX + "%s", rsaKey.getKeyID());
        return stringRedisTemplate.opsForValue()
                .set(redisQueryFriendlyKeyId, rsaPulicKeyJsonString, timeToLiveProperties.JWK())
                .filter(isSaved -> isSaved)
                .doOnNext(isSaved -> log.info("Saved rsa public key {} in redis", rsaPulicKeyJsonString))
                .switchIfEmpty(Mono.error(new IllegalStateException("Unable to store public key")))
                .doOnError(throwable -> log.error("Failed saving a rsa public key {} in redis", rsaPulicKeyJsonString, throwable));
    }

    private JWSHeader createSigningHeader(final RSAKey signingKey) {
        return new JWSHeader.Builder(RS256)
                .keyID(signingKey.getKeyID())
                .type(JOSEObjectType.JWT)
                .build();
    }

    public Mono<SignedJWT> sign(final JWTClaimsSet claimsSet) {
        return getRSAKey()
                .mapNotNull(rsaKey -> {
                    try {
                        SignedJWT jwt = new SignedJWT(createSigningHeader(rsaKey), claimsSet);
                        jwt.sign(new RSASSASigner(rsaKey));
                        return jwt;
                    } catch (JOSEException e) {
                        log.error("Failed to sign claim set {} with key {}", claimsSet, rsaKey.getKeyID(), e);
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Failed to sign", e);
                    }
                });
    }

    public Mono<JWKSet> getJWKSet() {
        return stringRedisTemplate.keys(JWK_KEY_PREFIX + "*")
                .flatMap(stringRedisTemplate.opsForValue()::get)
                .mapNotNull(rsaPulicKeyJsonString -> {
                    try {
                        return RSAKey.parse(rsaPulicKeyJsonString);
                    } catch (ParseException e) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Couldn't parse the public key %s".formatted(rsaPulicKeyJsonString), e);
                    }
                })
                .doOnError(throwable ->
                        log.error("Failed to parse one of the public keys after fetching it from redis", throwable)
                )
                .map(JWK.class::cast)
                .collectList()
                .map(JWKSet::new);
    }
}
