package dev.project.scholar_ai.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Information about a paper author")
public record AuthorDetailsDto(
    
    @Schema(description = "Author's full name", example = "Dr. Jane Smith")
    String name,
    
    @Schema(description = "Author's affiliation/institution", example = "MIT Computer Science Department")
    String affiliation,
    
    @Schema(description = "Author's email address", example = "jane.smith@mit.edu")
    String email
) {} 