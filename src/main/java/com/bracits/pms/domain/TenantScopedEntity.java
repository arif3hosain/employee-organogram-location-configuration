package com.bracits.pms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.TenantId;

/**
 * Base class of PARENT tables: tables that are listed, searched or referenced on their own.
 * Hibernate fills tenant_id on insert from the current tenant and adds {@code tenant_id = ?} to every
 * HQL/JPQL query on the entity, so services and repositories never pass the tenant by hand.
 * <p>
 * Child tables do not extend this: they are marked {@link TenantChildOf} and inherit the parent's tenant.
 * Reference data shared by every tenant is marked {@link SharedAcrossTenants}.
 */
@MappedSuperclass
@Getter
public abstract class TenantScopedEntity extends BaseEntity {

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;
}
