package com.bracits.pms.shared.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller -> Action -> Service (Technical Migration Guide §3.4).
 *
 * <pre>
 * run(input)
 *   1. preCondition(input)  -> error? return failure and STOP (execute never runs)
 *   2. execute(input)       -> calls exactly ONE @Transactional service method
 *   3. postCondition(...)   -> runs only after execute succeeded (mail, messages)
 * </pre>
 *
 * Rules (checked by ArchitectureTest):
 * <ul>
 *   <li>Actions are never {@code @Transactional}; only service methods are.</li>
 *   <li>Actions never use repositories; they call services only.</li>
 *   <li>If execute throws, the service transaction rolls back and postCondition is skipped.
 *       The exception is turned into a failure Result by GlobalExceptionHandler.</li>
 * </ul>
 *
 * @param <I> input (form / command / query)
 * @param <O> output returned by execute (usually a view DTO)
 */
public abstract class BaseAction<I, O> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public final Result<O> run(I input) {
        String error = preCondition(input);
        if (error != null) {
            log.debug("preCondition failed: {}", error);
            return Result.fail(error);                    // stop here, execute() never runs
        }

        O output = execute(input);                        // transaction commits inside the service
        postCondition(input, output);                     // only reached when execute succeeded
        return Result.ok(successMessage(output), output);
    }

    /** Validation, access and window checks. Return null when valid, otherwise an error message. */
    protected String preCondition(I input) {
        return null;
    }

    /** Must call exactly one transactional service method. */
    protected abstract O execute(I input);

    /** Side effects after a successful commit: mail, notifications, audit messages. */
    protected void postCondition(I input, O output) {
    }

    /** Message placed in Result.message on success. */
    protected String successMessage(O output) {
        return null;
    }

    /* ---------- small helpers for preCondition checks ---------- */

    protected static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    protected static boolean tooLong(String s, int max) {
        return s != null && s.length() > max;
    }
}
