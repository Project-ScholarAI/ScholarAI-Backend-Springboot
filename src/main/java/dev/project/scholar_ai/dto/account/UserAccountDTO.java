package dev.project.scholar_ai.dto.account;

import lombok.Data;

@Data
public class UserAccountDTO {
    private String fullName;
    private String institution;
    private String department;
    private String position;
    private String bio;

    private String profileImageUrl;
    private String profileImageFilename;

    private String websiteUrl;
    private String googleScholarUrl;
    private String linkedinUrl;
    private String githubUrl;
    private String facebookUrl;
    private String orcidId;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateProvinceRegion;
    private String postalCode;
    private String country;

    private String languagePreference;
    private String timezone;
}
