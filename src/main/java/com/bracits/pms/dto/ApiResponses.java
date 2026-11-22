package com.bracits.pms.dto;

import com.bracits.pms.dto.record.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Builds successful HTTP responses in the {@link ApiResponse} envelope. Errors are built by GlobalExceptionHandler. */
public final class ApiResponses {

    private ApiResponses() {
    }

    /** 200 with data only (reads). */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /** 200 with a user-facing message (updates, deletes). */
    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.ok(message, data));
    }

    /** 201 with a user-facing message (creates). */
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(message, data));
    }
}
