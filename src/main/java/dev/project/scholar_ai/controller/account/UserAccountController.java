package dev.project.scholar_ai.controller.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.project.scholar_ai.dto.account.UserAccountDTO;
import dev.project.scholar_ai.dto.common.APIResponse;
import dev.project.scholar_ai.model.core.account.UserAccount;
import dev.project.scholar_ai.service.account.UserAccountService;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class UserAccountController {
    private static final Logger logger = LoggerFactory.getLogger(UserAccountController.class);
    private final UserAccountService userAccountService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<APIResponse<UserAccount>> getAccount(Principal principal) {
        try {
            String email = principal.getName();
            logger.info("Get account endpoint hitted with email: {}", email);
            UserAccount userAccount = userAccountService.getAccountByEmail(email);
            logger.info("Fetched user account details: {}", objectMapper.writeValueAsString(userAccount));

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Account fetched successfully", userAccount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        }
    }

    @PatchMapping
    public ResponseEntity<APIResponse<UserAccount>> updateAccount(
            Principal principal, @RequestBody UserAccountDTO userAccountDTO) {
        try {
            String email = principal.getName();
            logger.info("Update account endpoint hitted with email: {}", email);
            UserAccount userAccount = userAccountService.getAccountByEmail(email);
            UserAccount updatedAccount = userAccountService.updateAccount(userAccount.getId(), userAccountDTO);
            logger.info("Updated user account details: {}", objectMapper.writeValueAsString(updatedAccount));

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Account updated successfully", updatedAccount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }

    @PostMapping("/profile-image")
    public ResponseEntity<APIResponse<Map<String, String>>> uploadImage(
            Principal principal, @RequestParam("profileImage") MultipartFile file) throws IOException {
        return null;
    }

    @DeleteMapping("/profile-image")
    public ResponseEntity<APIResponse<String>> deleteImage(Principal principal) {
        return null;
    }

    private String uploadToStorage(MultipartFile file) {
        // placeholder logic; actual file upload
        return null;
    }
}
