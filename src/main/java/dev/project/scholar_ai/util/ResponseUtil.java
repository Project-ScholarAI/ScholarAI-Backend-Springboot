package dev.project.scholar_ai.util;

import dev.project.scholar_ai.dto.common.ResponseWrapper;
import dev.project.scholar_ai.exception.ApiErrorResponse;
import dev.project.scholar_ai.exception.ErrorCode;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for creating standardized API responses.
 */
public class ResponseUtil {

    private ResponseUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates an ApiErrorResponse with the given parameters.
     */
    public static ApiErrorResponse createErrorResponse(
            HttpStatus status, ErrorCode errorCode, String message, String path) {
        return ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(errorCode.name())
                .message(message)
                .path(path)
                .suggestion(errorCode.getSuggestion())
                .build();
    }

    /**
     * Creates a success response with data.
     */
    public static <T> ResponseEntity<ResponseWrapper<T>> success(T data) {
        return ResponseEntity.ok(ResponseWrapper.success(data));
    }

    /**
     * Creates a success response with data and custom status.
     */
    public static <T> ResponseEntity<ResponseWrapper<T>> success(T data, HttpStatus status) {
        return new ResponseEntity<>(ResponseWrapper.success(data), status);
    }

    /**
     * Creates an error response.
     */
    public static <T> ResponseEntity<ResponseWrapper<T>> error(
            HttpStatus status, ErrorCode errorCode, String message, String path) {
        return ResponseEntity.status(status)
                .body(ResponseWrapper.error(createErrorResponse(status, errorCode, message, path)));
    }
}
