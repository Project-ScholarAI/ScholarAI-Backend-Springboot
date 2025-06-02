package dev.project.scholar_ai.controller.auth;

import dev.project.scholar_ai.dto.auth.AuthDTO;
import dev.project.scholar_ai.dto.auth.AuthResponse;
import dev.project.scholar_ai.dto.auth.RefreshTokenRequest;
import dev.project.scholar_ai.dto.common.APIResponse;
import dev.project.scholar_ai.service.auth.AuthService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RateLimiter(name = "standard-api")
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<APIResponse<String>> register(
            @Valid @RequestBody AuthDTO authDTO, HttpServletRequest request) {
        try {
            authService.registerUser(authDTO.getEmail(), authDTO.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(APIResponse.success(HttpStatus.CREATED.value(), "User registered successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), "Registration failed: "+ e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<AuthResponse>> login(
            @Valid @RequestBody AuthDTO authDTO,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            AuthResponse authResponse =
                    authService.loginUser(authDTO.getEmail(), authDTO.getPassword());

            //Create secure HttpOnly cookie for refresh token
            Cookie refreshCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
            refreshCookie.setHttpOnly(true);//only over https
            refreshCookie.setPath("/api/v1/auth/refresh");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);

            response.addCookie(refreshCookie);

            //remove refresh token from body before sending
            authResponse.setRefreshToken(null);

            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Login successful", authResponse));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password", null));
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Login error: "+ e.getMessage(), null));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<APIResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshRequest, HttpServletRequest servletRequest) {
        try {
            AuthResponse refreshed = authService.refreshToken(refreshRequest.getRefreshToken());
            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Token refreshed successfully", refreshed));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Invalid refresh token", null));
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Refresh error: "+ e.getMessage(), null));
        }}

    @PostMapping("/logout")
    public ResponseEntity<APIResponse<String>> logout(Principal principal) {
        try{
            String email = principal.getName();
            authService.logoutUser(email);
            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Logged out successfully", null));
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Logout failed: "+ e.getMessage(), null));
        }}


    // login by google
    @PostMapping("/social-login")
    public ResponseEntity<APIResponse<AuthResponse>>loginWithGoogle(@RequestBody Map<String, String> payload) {
       try{
           String idToken = payload.get("idToken");
           AuthResponse  response = authService.loginWithGoogle(idToken);
           return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Google login successful", response));
       }
       catch (BadCredentialsException e)
       {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                   .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Google login failed: "+ e.getMessage(), null));
       }
       catch (Exception e)
       {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error: " + e.getMessage() , null));
       }
    }
}
