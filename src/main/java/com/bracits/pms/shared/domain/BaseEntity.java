package com.bracits.pms.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.Getter;

import java.time.LocalDateTime;

/** Common id, optimistic-lock version and audit timestamps for every table. */
@MappedSuperclass
@Getter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optimistic locking (guide §3.4 rule 5). Clients send it back on update. */
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Rejects an update made from a stale copy. JPA's @Version check only compares the version
     * loaded in this transaction, so the version the client last saw must be compared explicitly.
     * A null expected version skips the check (POC convenience; make it mandatory in the product).
     */
    public void assertVersion(Long expected) {
        if (expected != null && !expected.equals(version)) {
            throw new com.bracits.pms.shared.error.BusinessException(
                    "The record was changed by someone else (version " + version + ", you sent " + expected + "). Reload and try again.");
        }
    }
}
