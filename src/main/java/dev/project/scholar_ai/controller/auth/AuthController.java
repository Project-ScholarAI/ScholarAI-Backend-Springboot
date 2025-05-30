package dev.project.scholar_ai.controller.auth;

import dev.project.scholar_ai.dto.auth.AuthDTO;
import dev.project.scholar_ai.dto.auth.AuthResponse;
import dev.project.scholar_ai.dto.auth.RefreshTokenRequest;
import dev.project.scholar_ai.dto.common.ResponseWrapper;
import dev.project.scholar_ai.exception.ErrorCode;
import dev.project.scholar_ai.service.auth.AuthService;
import dev.project.scholar_ai.util.ResponseUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.saml2.LogoutRequestDsl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RateLimiter(name = "standard-api")
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ResponseWrapper<String>> register(
            @Valid @RequestBody AuthDTO authDTO, HttpServletRequest request) {
        try {
            authService.registerUser(authDTO.getEmail(), authDTO.getPassword());
            return ResponseUtil.success("User registered successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseUtil.error(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDATION_ERROR,
                    "Registration failed: " + e.getMessage(),
                    request.getRequestURI());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<AuthResponse>> login(
            @Valid @RequestBody AuthDTO authDTO, HttpServletRequest request) {
        try {
            ResponseEntity<AuthResponse> authResponse =
                    authService.loginUser(authDTO.getEmail(), authDTO.getPassword());
            return ResponseUtil.success(authResponse.getBody());
        } catch (BadCredentialsException e) {
            return ResponseUtil.error(
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.ACCESS_DENIED,
                    "Invalid email or password",
                    request.getRequestURI());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseWrapper<AuthResponse>>refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshRequest, HttpServletRequest servletRequest){

        try{
            AuthResponse refreshed = authService.refreshToken(refreshRequest.getRefreshToken());
            return ResponseUtil.success(refreshed);
        }
        catch (BadCredentialsException e)
        {
            return ResponseUtil.error(
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.ACCESS_DENIED,
                    "Refresh failed"+
                    e.getMessage(),
                    servletRequest.getRequestURI());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseWrapper<String>>logout(Principal principal){
        String email = principal.getName();
        authService.logoutUser(email);
        return  ResponseUtil.success("Logged out successfully");
    }

    //login by google
    @PostMapping("/social-login")
    public ResponseEntity<AuthResponse> loginWithGoogle(@RequestBody Map<String, String> payload) {
        String idToken = payload.get("idToken");
        return authService.loginWithGoogle(idToken);
    }

}
