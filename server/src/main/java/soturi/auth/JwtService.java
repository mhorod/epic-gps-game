package soturi.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.time.Duration;
import java.time.Instant;

@Component
class JwtService {
    private static final Duration accessTokenValidity = Duration.ofDays(3);

    private final Algorithm algorithm = getAlgorithm();
    private final JWTVerifier verifier = JWT.require(algorithm).build();

    @SneakyThrows
    private static Algorithm getAlgorithm() {
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(spec);
        KeyPair pair = gen.generateKeyPair();
        return Algorithm.RSA512((RSAPublicKey) pair.getPublic(), (RSAPrivateKey) pair.getPrivate());
    }

    String createToken(String userName) {
       return JWT.create()
               .withClaim("username", userName)
               .withExpiresAt(Instant.now().plus(accessTokenValidity))
               .sign(algorithm);
    }

    boolean isTokenValid(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException ignored) {
            return false;
        }
    }

    String getUsername(String token) {
        try {
            DecodedJWT decoded = verifier.verify(token);
            return decoded.getClaim("username").asString();
        } catch (JWTVerificationException ignored) {
            return null;
        }
    }
}
