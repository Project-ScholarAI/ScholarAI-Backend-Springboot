package dev.project.scholar_ai.controller.auth;

import dev.project.scholar_ai.dto.auth.AuthDTO;
import dev.project.scholar_ai.dto.auth.AuthResponse;
import dev.project.scholar_ai.dto.common.APIResponse;
import dev.project.scholar_ai.service.auth.AuthService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RateLimiter(name = "standard-api")
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    // register new user
    @PostMapping("/register")
    public ResponseEntity<APIResponse<String>> register(
            @Valid @RequestBody AuthDTO authDTO, HttpServletRequest request) {
        try {
            logger.info("/register endpoint hitted with request, ", request);

            authService.registerUser(authDTO.getEmail(), authDTO.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(APIResponse.success(HttpStatus.CREATED.value(), "User registered successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(
                            HttpStatus.BAD_REQUEST.value(), "Registration failed: " + e.getMessage(), null));
        }
    }

    // login registered user
    @PostMapping("/login")
    public ResponseEntity<APIResponse<AuthResponse>> login(
            @Valid @RequestBody AuthDTO authDTO, HttpServletRequest request, HttpServletResponse response) {
        try {
            logger.info("login endpoint hitted with request,", request);

            AuthResponse authResponse = authService.loginUser(authDTO.getEmail(), authDTO.getPassword());

            logger.info("got auth response from authService for login request");

            // Create secure HttpOnly cookie for refresh token
            Cookie refreshCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
            refreshCookie.setHttpOnly(true); // only over https
            refreshCookie.setSecure(false); // for local testing
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);

            response.addCookie(refreshCookie);

            // Note: Refresh token is stored securely in HttpOnly cookie
            // For development/testing, you can keep it in response by commenting the next
            // line
             //authResponse.setRefreshToken(null);

            logger.info("response cookie added , authResponse: ", authResponse);
            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Login successful", authResponse));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password", null));
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Login error: " + e.getMessage(), null));
        }
    }

    // refresh access token when access token expires
    @PostMapping("/refresh")
    public ResponseEntity<APIResponse<AuthResponse>> refreshToken(
            HttpServletRequest request, HttpServletResponse response) {
        try {
            logger.info("refresh endpoint hitted");

            // Extract refresh token from cookies
            String refreshToken = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (refreshToken == null) {
                throw new BadCredentialsException("Missing refresh token");
            }

            AuthResponse refreshed = authService.refreshToken(refreshToken);

            // Set new refresh token in cookie
            Cookie refreshCookie = new Cookie("refreshToken", refreshed.getRefreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false); // for local testing
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(refreshCookie);

            // Remove refresh token from response body
            refreshed.setRefreshToken(null);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Token refreshed successfully", refreshed));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Invalid refresh token", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Refresh error: " + e.getMessage(), null));
        }
    }

    // logout user
    @PostMapping("/logout")
    public ResponseEntity<APIResponse<String>> logout(
            HttpServletRequest request, HttpServletResponse response, Principal principal) {
        try {
            logger.info("logout endpoint hitted");

            String email = principal.getName();
            authService.logoutUser(email);

            // Clear the cookie
            Cookie cookie = new Cookie("refreshToken", null);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(0);

            response.addCookie(cookie);

            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Logged out successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Logout failed: " + e.getMessage(), null));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            authService.sendResetCodeByMail(email);
            return ResponseEntity.ok(APIResponse.success(
                    HttpStatus.OK.value(), "Reset code sent to your email if the account exists.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String newPassword) {
        try {
            authService.verifyCodeAndResetPassword(email, code, newPassword);
            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Password reset successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }
}
