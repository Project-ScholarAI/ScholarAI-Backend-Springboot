package dev.project.scholar_ai.service.account;

import dev.project.scholar_ai.dto.account.UserAccountDTO;
import dev.project.scholar_ai.model.core.account.UserAccount;
import dev.project.scholar_ai.repository.core.account.UserAccountRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    /**
     * Fetch the user account profile by user ID.
     */
    public UserAccount getAccount(UUID userId) {
        return userAccountRepository
                .findById(userId)
                .orElseThrow(() -> new RuntimeException(("User profile not found for ID: " + userId)));
    }

    /**
     * Fetch the user account by email.
     */
    public UserAccount getAccountByEmail(String email) {
        return userAccountRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User profile not found for email: " + email));
    }

    /**
     * Update account fields using the DTO (partial update).
     */
    public UserAccount updateAccount(UUID userId, UserAccountDTO userAccountDTO) {
        UserAccount userAccount = getAccount(userId);

        // update only if null
        if (userAccountDTO.getFullName() != null) userAccount.setFullName(userAccountDTO.getFullName());
        if (userAccountDTO.getInstitution() != null) userAccount.setInstitution(userAccountDTO.getInstitution());
        if (userAccountDTO.getDepartment() != null) userAccount.setDepartment(userAccountDTO.getDepartment());
        if (userAccountDTO.getPosition() != null) userAccount.setPosition(userAccountDTO.getPosition());
        if (userAccountDTO.getBio() != null) userAccount.setBio(userAccountDTO.getBio());
        if (userAccountDTO.getWebsiteUrl() != null) userAccount.setWebsiteUrl(userAccountDTO.getWebsiteUrl());
        if (userAccountDTO.getGoogleScholarUrl() != null)
            userAccount.setGoogleScholarUrl(userAccountDTO.getGoogleScholarUrl());
        if (userAccountDTO.getLinkedinUrl() != null) userAccount.setLinkedinUrl(userAccountDTO.getLinkedinUrl());
        if (userAccountDTO.getGithubUrl() != null) userAccount.setGithubUrl(userAccountDTO.getGithubUrl());
        if (userAccountDTO.getFacebookUrl() != null) userAccount.setFacebookUrl(userAccountDTO.getFacebookUrl());
        if (userAccountDTO.getOrcidId() != null) userAccount.setOrcidId(userAccountDTO.getOrcidId());
        if (userAccountDTO.getAddressLine1() != null) userAccount.setAddressLine1(userAccountDTO.getAddressLine1());
        if (userAccountDTO.getAddressLine2() != null) userAccount.setAddressLine2(userAccountDTO.getAddressLine2());
        if (userAccountDTO.getCity() != null) userAccount.setCity(userAccountDTO.getCity());
        if (userAccountDTO.getStateProvinceRegion() != null)
            userAccount.setStateProvinceRegion(userAccountDTO.getStateProvinceRegion());
        if (userAccountDTO.getPostalCode() != null) userAccount.setPostalCode(userAccountDTO.getPostalCode());
        if (userAccountDTO.getCountry() != null) userAccount.setCountry(userAccountDTO.getCountry());
        if (userAccountDTO.getLanguagePreference() != null)
            userAccount.setLanguagePreference(userAccountDTO.getLanguagePreference());
        if (userAccountDTO.getTimezone() != null) userAccount.setTimezone(userAccountDTO.getTimezone());

        userAccount.setUpdatedAt(Instant.now());

        return userAccountRepository.save(userAccount);
    }

    /**
     * Update the profile image.
     */
    public void updateProfileImage(UUID userId, String imageUrl, String fileName) {
        UserAccount userAccount = getAccount(userId);
        userAccount.setProfileImageUrl(imageUrl);
        userAccount.setProfileImageFilename(fileName);
        userAccount.setUpdatedAt(Instant.now());
        userAccountRepository.save(userAccount);
    }

    /**
     * Delete the profile image (sets fields to null).
     */
    public void deleteProfileImage(UUID userId) {
        UserAccount userAccount = getAccount(userId);
        userAccount.setProfileImageUrl(null);
        userAccount.setProfileImageFilename(null);
        userAccount.setUpdatedAt(Instant.now());
        userAccountRepository.save(userAccount);
    }
}
