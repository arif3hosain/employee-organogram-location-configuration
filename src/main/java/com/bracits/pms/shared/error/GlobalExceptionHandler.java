package com.bracits.pms.shared.error;

import com.bracits.pms.shared.api.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Turns exceptions into the {@link ApiResponse} envelope with a meaningful HTTP status:
 * 400 invalid input (Bean Validation, malformed JSON), 404 missing record, 409 broken business rule.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** {@code @Valid @RequestBody} failed. message = all messages; data = field -> message. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> invalidBody(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        e.getBindingResult().getGlobalErrors()
                .forEach(ge -> errors.putIfAbsent(ge.getObjectName(), ge.getDefaultMessage()));
        String message = errors.values().stream().filter(Objects::nonNull).distinct().collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiResponse.fail(message, errors));
    }

    /** Constraint annotations on @RequestParam / @PathVariable failed. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> invalidParams(HandlerMethodValidationException e) {
        String message = e.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull).distinct().collect(Collectors.joining("; "));
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound(NotFoundException e) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> business(BusinessException e) {
        return respond(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> optimisticLock(ObjectOptimisticLockingFailureException e) {
        return respond(HttpStatus.CONFLICT, "The record was changed by someone else. Reload and try again.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> integrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return respond(HttpStatus.CONFLICT, "The change violates a database constraint (duplicate code or record still in use).");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> badRequest(Exception e) {
        return respond(HttpStatus.BAD_REQUEST, "Malformed request: " + e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> noResource(NoResourceFoundException e) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> unexpected(Exception e) {
        if (e instanceof ErrorResponse er) {               // Spring MVC errors (405, 415, missing param...) keep their status
            return ResponseEntity.status(er.getStatusCode()).body(ApiResponse.fail(e.getMessage()));
        }
        log.error("Unexpected error", e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error. See server log.");
    }

    private static ResponseEntity<ApiResponse<Void>> respond(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.fail(message));
    }
}
