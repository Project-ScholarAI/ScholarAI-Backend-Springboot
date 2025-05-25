package dev.project.scholar_ai.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for handling JWT tokens.
 * This class provides methods for generating, validating, and parsing JWT
 * tokens.
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwt-secret}")
    private String jwtSecret;

    @Value("${spring.app.jwt-expiration-ms}")
    private int jwtExpirationMs;

    /**
     * Extracts the JWT token from the Authorization header of an HTTP request.
     *
     * @param request The HTTP servlet request.
     * @return The JWT token string if present and correctly formatted, otherwise
     *         null.
     */
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove Bearer prefix
        }
        return null;
    }

    /**
     * Generates a JWT token for the given user details.
     * @return A JWT token string.
     */

    public String generateAccessToken(String username)
    {
        return generateToken(username, accessTokenValidityMs);
    }

    public String generateRefreshToken(String username)
    {
        return generateToken(username, refreshTokenValidityMs);
    }

    public String generateToken(String username, long expirationMilis)
    {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMilis);

        return  Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }


    /**
     * Extracts the username from a JWT token.
     *
     * @param token The JWT token string.
     * @return The username contained in the token.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Generates the signing key for JWT tokens using the configured secret.
     *
     * @return The signing key.
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Validates a JWT token.
     * It checks for malformation, expiration, unsupported format, and empty claims.
     *
     * @param authToken The JWT token string to validate.
     * @return True if the token is valid, false otherwise.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            logger.debug("Validating JWT token: {}", authToken);
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
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
}
