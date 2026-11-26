package com.bracits.pms.tenant;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * pms.tenancy.header           request header carrying the tenant id (POC). In the product the tenant
 *                              comes from the signed-in user's token, never from a free header.
 * pms.tenancy.default-tenant-id used when the header is missing. Set in the POC so the current UI works
 *                              unchanged; leave empty in the product so a missing tenant is rejected.
 */
@ConfigurationProperties(prefix = "pms.tenancy")
public record TenancyProperties(String header, Long defaultTenantId) {

    public TenancyProperties {
        if (header == null || header.isBlank()) {
            header = "X-Tenant-Id";
        }
    }
}
