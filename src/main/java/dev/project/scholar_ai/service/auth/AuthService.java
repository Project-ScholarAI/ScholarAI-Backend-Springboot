package dev.project.scholar_ai.service.auth;

import dev.project.scholar_ai.dto.auth.AuthResponse;
import dev.project.scholar_ai.model.core.auth.AuthUser;
import dev.project.scholar_ai.repository.core.auth.AuthUserRepository;
import dev.project.scholar_ai.security.JwtUtils;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthUserRepository authUserRepository;
    private final UserLoadingService userLoadingService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

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

    // register new user
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

    // login registered user
    public AuthResponse loginUser(String email, String password) {

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
        return new AuthResponse(accessToken, refreshToken, userDetails.getUsername(), user.getId(), roles);
    }

    // refresh access token when access token expires
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtils.validateJwtToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String username = jwtUtils.getUserNameFromJwtToken(refreshToken);

        if (!refreshTokenService.isRefreshTokenValid(username, refreshToken)) {
            throw new BadCredentialsException("Refresh token is not recognized");
        }

        String newAccessToken = jwtUtils.generateAccessToken(username);
        String newRefreshToken = refreshToken;
        refreshTokenService.saveRefreshToken(username, newRefreshToken);

        AuthUser user = authUserRepository
                .findByEmail(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid Email..."));

        List<String> roles = userLoadingService.loadUserByUsername(username).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new AuthResponse(newAccessToken, newRefreshToken, username, user.getId(), roles);
    }

    // Logout user
    public void logoutUser(String username) {
        refreshTokenService.deleteRefreshToken(username);
    }

    // Forgot Password: generate and send reset code
    public void sendResetCodeByMail(String email) {
        AuthUser user = authUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("No user with that email."));

        String code = String.valueOf((int) ((Math.random() * 900000) + 100000)); // 6-digit code
        String redisKey = "RESET_CODE:" + email;
        redisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(10)); // expires in 10 min

        emailService.sendResetCodeByEmail(email, code);
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            transactionManager = "transactionManager"
    )
    // Reset Password: verify code and update password
    public void verifyCodeAndResetPassword(String email, String code, String newPassword) {
        String redisKey = "RESET_CODE:"+ email;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if(storedCode == null || !storedCode.equals(code)){
            throw new IllegalArgumentException("Invalid or expired reset code");
        }

        AuthUser user = authUserRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("User not found by this email!")
        );

        System.out.println("Before update: " + user.getEncryptedPassword());

        String encoded = passwordEncoder.encode(newPassword);
        user.setEncryptedPassword(encoded);
        authUserRepository.saveAndFlush(user);

        System.out.println("After update: " + encoded);
        System.out.println("User's password after update");
        System.out.println(user.getEncryptedPassword());

        redisTemplate.delete(redisKey);//invalidate used code
        redisTemplate.delete("REFRESH_TOKEN:" + email);
    }
}
