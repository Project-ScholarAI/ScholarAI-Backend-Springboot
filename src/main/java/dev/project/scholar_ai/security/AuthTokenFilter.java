package dev.project.scholar_ai.security;

import dev.project.scholar_ai.service.auth.UserLoadingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter for authenticating users based on JWT tokens in the Authorization
 * header.
 * This filter intercepts incoming requests, extracts and validates the JWT
 * token,
 * and sets the authentication in the Spring Security context if the token is
 * valid.
 */
@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserLoadingService userLoadingService;
    private static final Logger log = LoggerFactory.getLogger(AuthTokenFilter.class);

    /**
     * Performs the filtering logic for each request.
     * It extracts the JWT from the request, validates it, and if valid,
     * loads the user details and sets the authentication in the security context.
     *
     * @param request     The HTTP servlet request.
     * @param response    The HTTP servlet response.
     * @param filterChain The filter chain.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Check for the custom header to bypass authentication
        log.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());
        try {
            String accessToken = parseJwt(request);
            if (accessToken != null && jwtUtils.validateJwtToken(accessToken)) {
                String username = jwtUtils.getUserNameFromJwtToken(accessToken);
                log.debug("Username: {}", username);

                UserDetails userDetails = userLoadingService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                log.debug("Roles from JWT: {}", userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Parses the JWT token from the Authorization header of the request.
     *
     * @param request The HTTP servlet request.
     * @return The JWT token string, or null if not found or not correctly
     *         formatted.
     */
    private String parseJwt(HttpServletRequest request) {
        String accessToken = jwtUtils.getJwtFromHeader(request);
        log.debug("AuthTokenFilter.java: {}", accessToken);
        return accessToken;
    }
}
