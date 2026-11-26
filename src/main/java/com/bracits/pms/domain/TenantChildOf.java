package com.bracits.pms.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A CHILD table: no tenant_id column. Its rows belong to the tenant of the parent row and must only be
 * read through the parent (or with a query that joins the parent). Its repository must not expose
 * findById/findAll (checked by TenancyRulesTest). Give it its own tenant_id later only if it starts
 * being listed or searched on its own.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TenantChildOf {

    /** The parent entity whose tenant this table inherits. */
    Class<? extends TenantScopedEntity> value();
}
