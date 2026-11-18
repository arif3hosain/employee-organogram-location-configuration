package com.bracits.pms.shared.action;

/**
 * Outcome of an action, returned to the controller as JSON.
 * Matches the guide's Result (success + message) and adds an optional payload.
 */
public record Result<T>(boolean success, String message, T data) {

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(true, message, data);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(true, null, data);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(false, message, null);
    }
}
