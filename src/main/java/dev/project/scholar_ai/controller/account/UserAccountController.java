package dev.project.scholar_ai.controller.account;

import dev.project.scholar_ai.dto.account.UserAccountDTO;
import dev.project.scholar_ai.dto.common.APIResponse;
import dev.project.scholar_ai.model.core.account.UserAccount;
import dev.project.scholar_ai.repository.core.auth.AuthUserRepository;
import dev.project.scholar_ai.service.account.UserAccountService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.atn.SemanticContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class UserAccountController {
    private final UserAccountService userAccountService;
    private final AuthUserRepository authUserRepository;

    private UUID getUserIdFromPrincipal(Principal principal){
        String email = principal.getName();
        return authUserRepository.findByEmail(email).orElseThrow(
                ()->new RuntimeException("User not found: "+ email)).getId();
    }


    @GetMapping
    public ResponseEntity<APIResponse<UserAccount>>getAccount(Principal principal)
    {
        try{
            UUID userId = getUserIdFromPrincipal(principal);

            //Get User Account
            return null;
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        }
    }

    @PatchMapping
    public ResponseEntity<APIResponse<UserAccount>>updateAccount(Principal principal, @RequestBody UserAccountDTO userAccountDTO){
        return null;
    }

    @PostMapping("/profile-image")
    public ResponseEntity<APIResponse<Map<String, String>>>uploadImage(
            Principal principal, @RequestParam("profileImage") MultipartFile file)throws IOException
    {
        return null;
    }

    @DeleteMapping("/profile-image")
    public ResponseEntity<APIResponse<String>>deleteImage(Principal principal){
        return null;
    }

    private String uploadToStorage(MultipartFile file){
        //placeholder logic; actual file upload
        return null;
    }

}
