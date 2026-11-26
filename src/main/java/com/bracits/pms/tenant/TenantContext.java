package com.bracits.pms.tenant;

import java.util.Optional;

/**
 * The tenant of the current request (or job). Set by {@link TenantFilter} for every /api call and
 * cleared afterwards. Background jobs and sync must call {@link #run(Long, Runnable)} explicitly:
 * there is no implicit "all tenants" mode.
 */
public final class TenantContext {

    /** Used when no tenant is set: matches no row and fails every insert (fail closed). */
    public static final Long NO_TENANT = -1L;

    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static Optional<Long> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    /** The current tenant, or an exception if none is set. */
    public static Long require() {
        Long id = CURRENT.get();
        if (id == null) {
            throw new IllegalStateException("No tenant in context");
        }
        return id;
    }

    static void set(Long tenantId) {
        CURRENT.set(tenantId);
    }

    static void clear() {
        CURRENT.remove();
    }

    /** Runs work as one tenant (jobs, sync, tests), restoring whatever was set before. */
    public static void run(Long tenantId, Runnable work) {
        Long previous = CURRENT.get();
        CURRENT.set(tenantId);
        try {
            work.run();
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }
}
