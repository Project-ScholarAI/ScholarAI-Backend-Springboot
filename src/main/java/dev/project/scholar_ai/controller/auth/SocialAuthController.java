package dev.project.scholar_ai.controller.auth;

import dev.project.scholar_ai.dto.auth.AuthResponse;
import dev.project.scholar_ai.dto.common.APIResponse;
import dev.project.scholar_ai.service.auth.SocialAuthService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RateLimiter(name = "standard-api")
@RequestMapping("api/v1/auth/social")
@RequiredArgsConstructor
public class SocialAuthController {
    private static final Logger logger = LoggerFactory.getLogger(SocialAuthController.class);
    private final SocialAuthService socialAuthService;

    // login by google
    @PostMapping("/google-login")
    public ResponseEntity<APIResponse<AuthResponse>> loginWithGoogle(
            @RequestBody Map<String, String> payload,
            HttpServletResponse httpServletResponse) { // Inject HttpServletResponse
        try {
            String idToken = payload.get("idToken");
            logger.info("social-login hits with idToken: '{}'", idToken);

            if (idToken == null || idToken.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), "ID token is missing.", null));
            }

            AuthResponse authResponseFromService = socialAuthService.loginWithGoogle(idToken);

            // Set the refreshToken as an HttpOnly cookie
            if (authResponseFromService.getRefreshToken() != null
                    && !authResponseFromService.getRefreshToken().isEmpty()) {
                Cookie refreshCookie = new Cookie("refreshToken", authResponseFromService.getRefreshToken());
                refreshCookie.setHttpOnly(true);
                refreshCookie.setSecure(false); // TODO: Set to true in production (HTTPS)
                refreshCookie.setPath("/");
                refreshCookie.setMaxAge(7 * 24 * 60 * 60); // Your refresh token's validity in seconds
                httpServletResponse.addCookie(refreshCookie);

                // Nullify the refresh token in the body as it's now in a secure cookie
                authResponseFromService.setRefreshToken(null);
            } else {
                // This indicates an issue if refresh token rotation/issuance is expected
                logger.warn("Refresh token was not provided by authService.loginWithGoogle() for social login.");
            }

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Google login successful", authResponseFromService));

        } catch (BadCredentialsException e) {
            logger.warn("Google login failed (BadCredentialsException): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.error(
                            HttpStatus.UNAUTHORIZED.value(), "Google login failed: " + e.getMessage(), null));
        } catch (IllegalArgumentException e) { // Catch specific exceptions if id token validation fails
            logger.warn("Google ID token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.error(
                            HttpStatus.UNAUTHORIZED.value(), "Invalid Google ID token: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Unexpected error during Google social login: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error during Google login.", null));
        }
    }

    @PostMapping("/github-login")
    public ResponseEntity<APIResponse<AuthResponse>> loginWithGithub(
            @RequestBody Map<String, String> payload, HttpServletResponse httpServletResponse) {
        try {
            logger.info("github-login endpoint hits");
            String code = payload.get("code");

            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(APIResponse.error(
                                HttpStatus.BAD_REQUEST.value(), "GitHub authorization code is missing", null));
            }

            AuthResponse authResponse = socialAuthService.loginWithGithub(code);

            if (authResponse.getRefreshToken() != null
                    && !authResponse.getRefreshToken().isEmpty()) {
                Cookie refreshCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
                refreshCookie.setHttpOnly(true);
                refreshCookie.setSecure(false); // Change to true in production
                refreshCookie.setPath("/");
                refreshCookie.setMaxAge(7 * 24 * 60 * 60);
                httpServletResponse.addCookie(refreshCookie);
                authResponse.setRefreshToken(null); // remove from body
            }

            logger.info("cookie set for github auth");
            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "GitHub login successful", authResponse));

        } catch (BadCredentialsException e) {
            logger.warn("GitHub login failed (BadCredentialsException): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.error(
                            HttpStatus.UNAUTHORIZED.value(), "GitHub login failed: " + e.getMessage(), null));
        } catch (IllegalStateException e) {
            logger.warn("GitHub token exchange failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(
                            HttpStatus.BAD_REQUEST.value(),
                            "Failed to get access token from GitHub: " + e.getMessage(),
                            null));
        } catch (Exception e) {
            logger.error("Unexpected error during GitHub login: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Unexpected error during GitHub login: " + e.getMessage(),
                            null));
        }
    }
}
