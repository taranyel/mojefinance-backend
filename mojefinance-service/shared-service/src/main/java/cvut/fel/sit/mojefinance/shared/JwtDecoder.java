package cvut.fel.sit.mojefinance.shared;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Component
@AllArgsConstructor
public class JwtDecoder {
    public static String extractUsernameFromJWT(String authorization) {
        String token = authorization.replaceFirst("(?i)^Bearer\\s+", "");
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            return claims.getStringClaim("preferred_username");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
