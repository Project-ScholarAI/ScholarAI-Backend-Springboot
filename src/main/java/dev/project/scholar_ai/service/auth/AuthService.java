package dev.project.scholar_ai.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import dev.project.scholar_ai.dto.auth.AuthResponse;
import dev.project.scholar_ai.model.auth.AuthUser;
import dev.project.scholar_ai.repository.auth.AuthUserRepository;
import dev.project.scholar_ai.security.GoogleVerifierUtil;
import dev.project.scholar_ai.security.JwtUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final UserLoadingService userLoadingService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    private GoogleVerifierUtil googleVerifierUtil; // Create this utility (code below)


    public Authentication authentication(String email, String password) {

        UserDetails userDetails = userLoadingService.loadUserByUsername(email);

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid email ...");
        }
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid Password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    //register new user
    public void registerUser(String email, String password) {
        if (authUserRepository.findByEmail(email).isPresent()) {
            throw new BadCredentialsException("User with email " + email + " already exists.");
        }
        AuthUser newUser = new AuthUser();
        newUser.setEmail(email);
        newUser.setEncryptedPassword(passwordEncoder.encode(password));
        newUser.setRole("USER"); // Default role
        authUserRepository.save(newUser);
    }

    //login registered user
    public ResponseEntity<AuthResponse> loginUser(String email, String password) {

        Authentication authentication = authentication(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtUtils.generateAccessToken(userDetails.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());
        refreshTokenService.saveRefreshToken(userDetails.getUsername(), refreshToken);

        AuthUser user = authUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email ..."));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        AuthResponse loginResponse = new AuthResponse(accessToken, refreshToken, userDetails.getUsername(), user.getId(), roles);
        loginResponse.setRefreshToken(refreshToken);
        return ResponseEntity.ok(loginResponse);
    }

    //refresh access token when refresh token expires
    public AuthResponse refreshToken(String refreshToken)
    {
        if(!jwtUtils.validateJwtToken(refreshToken)){
            throw  new BadCredentialsException("Invalid refresh token");
        }

        String username = jwtUtils.getUserNameFromJwtToken(refreshToken);

        if(!refreshTokenService.isRefreshTokenValid(username, refreshToken)){
            throw new BadCredentialsException("Refresh token is not recognized");
        }

        String newAccessToken = jwtUtils.generateAccessToken(username);
        String newRefreshToken = jwtUtils.generateRefreshToken(username);
        refreshTokenService.saveRefreshToken(username, newRefreshToken);

        AuthUser user = authUserRepository.findByEmail(username)
                .orElseThrow(()-> new BadCredentialsException("Invalid Email..."));

        List<String>roles = userLoadingService.loadUserByUsername(username).getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .toList();

        return new AuthResponse(newAccessToken,newRefreshToken, username, user.getId(), roles);
    }

    //Logout user
    public void logoutUser(String username)
    {
        refreshTokenService.deleteRefreshToken(username);
    }


    //login by google
    public ResponseEntity<AuthResponse> loginWithGoogle(String idTokenString) {
        // Step 1: Verify Google token
        GoogleIdToken.Payload payload = googleVerifierUtil.verify(idTokenString);
        if (payload == null) {
            throw new BadCredentialsException("Invalid Google ID token");
        }

        String email = payload.getEmail();

        // Step 2: Register if not exist
        AuthUser user = authUserRepository.findByEmail(email)
                .orElseGet(() -> {
                    AuthUser newUser = new AuthUser();
                    newUser.setEmail(email);
                    newUser.setEncryptedPassword(""); // No password for social login
                    newUser.setRole("USER");
                    return authUserRepository.save(newUser);
                });

        // Step 3: Generate tokens
        String accessToken = jwtUtils.generateAccessToken(email);
        String refreshToken = jwtUtils.generateRefreshToken(email);
        refreshTokenService.saveRefreshToken(email, refreshToken);

        List<String> roles = List.of(user.getRole());

        AuthResponse authResponse = new AuthResponse(accessToken, refreshToken, email, user.getId(), roles);
        return ResponseEntity.ok(authResponse);
    }

}
