package com.bracits.pms.shared.error;

/**
 * A business rule that failed inside the service transaction (guide §3.4 rule 5:
 * critical rules are re-checked in the service because data can change after preCondition).
 * Rolls the transaction back. Mapped to HTTP 409.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
