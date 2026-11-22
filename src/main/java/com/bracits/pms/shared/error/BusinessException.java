package com.bracits.pms.shared.error;

/**
 * A business rule that failed inside the service transaction (duplicate code, level rule,
 * reporting loop, record still in use...). Rolls the transaction back. Mapped to HTTP 409.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
