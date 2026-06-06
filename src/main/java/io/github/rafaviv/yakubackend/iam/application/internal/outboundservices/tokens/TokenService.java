package io.github.rafaviv.yakubackend.iam.application.internal.outboundservices.tokens;



/**
 * TokenService interface
 * This interface is used to generate and validate tokens
 */
public interface TokenService {

    /**
     * Generate a token for a given username and tenant ID
     * @param userId the user ID
     * @param userRole the user role
     * @return String the token
     */
    String generateToken(Long userId, String userRole);

    /**
     * Extract the username from a token
     * @param token the token
     * @return Long the userId
     */
    Long getUserIdFromToken(String token);

    /**
     * Extract the workshop ID from a token
     * @param token the token
     * @return Long the workshop ID
     */
    Long getWorkshopIdFromToken(String token);

    /**
     * Validate a token
     * @param token the token
     * @return boolean true if the token is valid, false otherwise
     */
    boolean validateToken(String token);
}