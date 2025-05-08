package dev.project.scholar_ai.dto.common;

import dev.project.scholar_ai.exception.ApiErrorResponse;
import lombok.Getter;

/**
 * Wrapper class for API responses that can handle both success and error cases.
 */
@Getter
public class ResponseWrapper<T> {
    private final boolean success;
    private final T data;
    private final ApiErrorResponse error;

    private ResponseWrapper(boolean success, T data, ApiErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ResponseWrapper<T> success(T data) {
        return new ResponseWrapper<>(true, data, null);
    }

    public static <T> ResponseWrapper<T> error(ApiErrorResponse error) {
        return new ResponseWrapper<>(false, null, error);
    }
}
