package ua.deti.tqs.components;


import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ua.deti.tqs.entities.User;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final int jwtExpirationMs;

    public JwtUtils(@Value("${greenev.app.jwtPublicKey}") String publicKeyString,
                    @Value("${greenev.app.jwtPrivateKey}") String privateKeyString,
                    @Value("${greenev.app.jwtExpirationMs}") int jwtExpirationMs) throws GeneralSecurityException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.publicKey = kf.generatePublic(getEncodedKeySpec(publicKeyString));
        this.privateKey = kf.generatePrivate(getEncodedPrivateKeySpec(privateKeyString));
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateJwtToken(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getEmail())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(privateKey)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return getClaimsFromJwtToken(token).getSubject();
    }

    public Date getExpirationFromJwtToken(String token) {
        return getClaimsFromJwtToken(token).getExpiration();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            this.getJwtParser().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    private X509EncodedKeySpec getEncodedKeySpec(String key) {
        byte[] byteKey = Base64.getMimeDecoder().decode(key.getBytes());
        return new X509EncodedKeySpec(byteKey);
    }

    private PKCS8EncodedKeySpec getEncodedPrivateKeySpec(String key) {
        byte[] byteKey = Base64.getMimeDecoder().decode(key.getBytes());
        return new PKCS8EncodedKeySpec(byteKey);
    }

    private JwtParser getJwtParser() {
        return Jwts.parser().verifyWith(publicKey).build();
    }

    private Claims getClaimsFromJwtToken(String token) {
        return this.getJwtParser()
                .parseSignedClaims(token)
                .getPayload();
    }
}