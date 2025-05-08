package dev.project.scholar_ai.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Standard error response structure for the API.
 */
@Data
@Builder
public class ApiErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime timestamp;

    private int status;
    private String error;
    private String code;
    private String message;
    private String path;
    private List<String> details;
    private String suggestion;
}
