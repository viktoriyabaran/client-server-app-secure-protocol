package pr3.server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import pr4.model.User;

import java.time.Instant;

public class JwtService {
    private static final Algorithm ALGORITHM = Algorithm.HMAC256("secretKeyCSA");
    private static final String MAGIC_STRING = "MagicStringCSA";
    public static String create(User user) {
        String token;
        try {
            token = JWT.create()
                    .withSubject(user.getUsername())
                    .withExpiresAt(Instant.now().plusSeconds(240))
                    .withClaim("email", user.getEmail())
                    .withClaim("magStr", MAGIC_STRING)
                    .sign(ALGORITHM);

            return token;
        } catch (JWTCreationException exception){
            throw new RuntimeException(exception);
        }
    }

    public static String verify(String token) {
        DecodedJWT decodedJWT;
        try {
            JWTVerifier verifier = JWT.require(ALGORITHM)
                    .withClaim("magStr", MAGIC_STRING)
                    .build();

            decodedJWT = verifier.verify(token);
        } catch (JWTVerificationException exception){
            throw new RuntimeException(exception);
        }
        return decodedJWT.getSubject();
    }
}
