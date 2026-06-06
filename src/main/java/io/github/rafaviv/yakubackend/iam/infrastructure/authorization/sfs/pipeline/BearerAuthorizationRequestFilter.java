package io.github.rafaviv.yakubackend.iam.infrastructure.authorization.sfs.pipeline;

import io.github.rafaviv.yakubackend.iam.infrastructure.authorization.sfs.model.UserDetailsServiceExtension;
import io.github.rafaviv.yakubackend.iam.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import io.github.rafaviv.yakubackend.iam.infrastructure.tokens.jwt.BearerTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Bearer Authorization Request Filter
 * <p>
 * This filter is responsible for processing the JWT token from the request header
 * and setting the authentication in the security context if the token is valid.
 * </p>
 */
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthorizationRequestFilter.class);

    private final BearerTokenService tokenService;
    private final UserDetailsServiceExtension userDetailsService;

    public BearerAuthorizationRequestFilter(BearerTokenService tokenService, UserDetailsServiceExtension userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        LOGGER.debug("Incoming request to: {}", path);

        // 2️⃣ Endpoints públicos (sin token requerido)
        if (isPublicPath(path)) {
            LOGGER.debug("🟢 Ruta pública detectada ({}), omitiendo validación JWT", path);
            filterChain.doFilter(request, response);
            return;
        }

        // 3️⃣ Validación normal del token
        String token = tokenService.getBearerTokenFrom(request);

        if (StringUtils.hasText(token)) {
            try {
                if (tokenService.validateToken(token)) {
                    Long userId = tokenService.getUserIdFromToken(token);
                    UserDetails userDetails = userDetailsService.loadUserById(userId);

                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    var authenticationToken = UsernamePasswordAuthenticationTokenBuilder.build(userDetails, request);
                    context.setAuthentication(authenticationToken);
                    SecurityContextHolder.setContext(context);
                }
            } catch (Exception e) {
                LOGGER.warn("Invalid JWT token for request to {}: {}", path, e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.contains("/api/v1/users/signup") ||
               path.contains("/api/v1/users/signin") ||
               path.contains("/api/v1/users/available-roles") ||
               path.contains("/v3/api-docs") ||
               path.contains("/swagger-ui") ||
               path.contains("/swagger-resources") ||
               path.contains("/webjars");
    }
} 