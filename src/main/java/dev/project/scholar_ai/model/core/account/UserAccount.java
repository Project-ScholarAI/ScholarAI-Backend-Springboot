package dev.project.scholar_ai.model.core.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;
import java.util.UUID;

@Entity
@Setter
@Getter
@DynamicUpdate
@Table(name = "user_accounts", schema = "public")
public class UserAccount {
    @Id
    @Column(nullable = false)
    private UUID id; // Links to users.id (auth.users in Supabase or users in your system)

    private String fullName;
    private String email;
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

    private Instant createdAt;
    private Instant updatedAt;
}
