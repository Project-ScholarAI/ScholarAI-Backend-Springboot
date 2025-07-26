package dev.project.scholar_ai.dto.papercall;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperCallResponse {
    private String title;
    private String link;
    private String type;
    private String source;
    private String whenHeld;
    private String whereHeld;
    private String deadline;
    private String description;
    private String domain;
    private LocalDateTime createdAt;
} 