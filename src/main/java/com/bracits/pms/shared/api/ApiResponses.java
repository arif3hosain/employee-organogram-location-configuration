package com.bracits.pms.shared.api;

import com.bracits.pms.shared.action.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Maps an action Result to an HTTP response. A failed preCondition becomes 400. */
public final class ApiResponses {

    private ApiResponses() {
    }

    public static <T> ResponseEntity<Result<T>> of(Result<T> result) {
        return of(result, HttpStatus.OK);
    }

    public static <T> ResponseEntity<Result<T>> created(Result<T> result) {
        return of(result, HttpStatus.CREATED);
    }

    private static <T> ResponseEntity<Result<T>> of(Result<T> result, HttpStatus successStatus) {
        return ResponseEntity.status(result.success() ? successStatus : HttpStatus.BAD_REQUEST).body(result);
    }
}
