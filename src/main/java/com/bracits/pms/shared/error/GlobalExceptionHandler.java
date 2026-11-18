package com.bracits.pms.shared.error;

import com.bracits.pms.shared.action.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Turns exceptions thrown from execute() (i.e. from inside a service transaction that
 * has already rolled back) into the same Result JSON shape the actions return.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Result<Void>> notFound(NotFoundException e) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> business(BusinessException e) {
        return respond(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Result<Void>> optimisticLock(ObjectOptimisticLockingFailureException e) {
        return respond(HttpStatus.CONFLICT, "The record was changed by someone else. Reload and try again.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> integrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return respond(HttpStatus.CONFLICT, "The change violates a database constraint (duplicate code or record still in use).");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Result<Void>> badRequest(Exception e) {
        return respond(HttpStatus.BAD_REQUEST, "Malformed request: " + e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> noResource(NoResourceFoundException e) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> unexpected(Exception e) {
        if (e instanceof ErrorResponse er) {               // Spring MVC errors (405, 415, missing param...) keep their status
            return ResponseEntity.status(er.getStatusCode()).body(Result.fail(e.getMessage()));
        }
        log.error("Unexpected error", e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error. See server log.");
    }

    private static ResponseEntity<Result<Void>> respond(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Result.fail(message));
    }
}
