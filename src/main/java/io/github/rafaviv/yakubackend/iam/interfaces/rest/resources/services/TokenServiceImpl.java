package io.github.rafaviv.yakubackend.iam.interfaces.rest.resources.services;

import io.github.rafaviv.yakubackend.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Token service implementation for JWT tokens.
 * This class is responsible for generating and validating JWT tokens.
 * It uses the secret and expiration days from the application.properties file.
 */
@Service
public class TokenServiceImpl implements BearerTokenService {
    private final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);

    private static final String AUTHORIZATION_PARAMETER_NAME = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private static final String WORKSHOP_ID_CLAIM = "workshop_id";
    private static final String ROLE_CLAIM = "role";
    private static final int TOKEN_BEGIN_INDEX = 7;

    @Value("${authorization.jwt.secret}")
    private String secret;

    @Value("${authorization.jwt.expiration.days}")
    private int expirationDays;


    /**
     * Generates a JWT token based on the user ID, user role, and optionally the workshop ID.
     * @param userId The user ID (used as the subject)
     * @param userRole The user's role
     * @return The generated JWT token
     */
    @Override
    public String generateToken(Long userId, String userRole) {
        var issuedAt = new Date();
        var expiration = DateUtils.addDays(issuedAt, expirationDays);
        var key = getSigningKey();

        var builder = Jwts.builder()
                .subject(userId.toString())
                .claim(ROLE_CLAIM, userRole)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key);

        return builder.compact();
    }


    @Override
    public Long getUserIdFromToken(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            LOGGER.error("Subject in token is not a valid Long ID: {}", subject);
            throw new SecurityException("Invalid user ID format in token subject.");
        }
    }

    @Override
    public Long getWorkshopIdFromToken(String token) {
        return extractClaim(token, claims -> claims.get(WORKSHOP_ID_CLAIM, Long.class));
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            LOGGER.info("Token is valid");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Logs and re-throws all security-related token exceptions
            LOGGER.error("JWT validation failed: {}", e.getMessage());
            throw e;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean isTokenPresentIn(String authorizationParameter) {
        return StringUtils.hasText(authorizationParameter);
    }


    private boolean isBearerTokenIn(String authorizationParameter) {
        return authorizationParameter.startsWith(BEARER_TOKEN_PREFIX);
    }

    private String extractTokenFrom(String authorizationHeaderParameter) {
        return authorizationHeaderParameter.substring(TOKEN_BEGIN_INDEX);
    }

    private String getAuthorizationParameterFrom(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION_PARAMETER_NAME);
    }

    @Override
    public String getBearerTokenFrom(HttpServletRequest request) {
        String parameter = getAuthorizationParameterFrom(request);

        if (isTokenPresentIn(parameter) && isBearerTokenIn(parameter)) return extractTokenFrom(parameter);
        return null;
    }
}
