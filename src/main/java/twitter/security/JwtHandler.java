package twitter.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.configuration.Value;

import java.security.Key;
import java.util.Date;

@Component
public class JwtHandler {

    @Value(key = "jwt.secret.key")
    private String secretKey;

    @Value(key = "jwt.token.life.time")
    private Long tokenLifeTime;

    @Injection
    public JwtHandler() {}

    public String generateToken(String username) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + tokenLifeTime);

        return Jwts
                .builder()
                .setIssuedAt(now)
                .setExpiration(expiredAt)
                .setSubject(username)
                .signWith(this.getSighInkey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(this.getSighInkey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(this.getSighInkey()).build().parse(token);
            return true;
        } catch (ExpiredJwtException ex) {
            System.out.println("Token expired");
        } catch (MalformedJwtException ex) {
            System.out.println("Token invalid");
        } catch (SignatureException ex) {
            System.out.println("Token Signature Incorrect");
        } catch (IllegalArgumentException ex) {
            System.out.println("Token is null or empty");
        }
        return false;
    }

    private Key getSighInkey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
