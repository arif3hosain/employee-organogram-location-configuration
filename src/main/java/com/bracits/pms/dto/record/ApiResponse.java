package com.bracits.pms.dto.record;

import com.bracits.pms.dto.ApiResponses;

/**
 * The JSON envelope every endpoint returns: {@code { success, message, data }}.
 * Built by {@link ApiResponses} on success and by GlobalExceptionHandler on failure.
 */
public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public static <T> ApiResponse<T> fail(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}
