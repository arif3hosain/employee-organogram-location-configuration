package com.bracits.pms.corehr.organogram.api;

import com.bracits.pms.corehr.organogram.domain.OrgUnitNature;
import com.bracits.pms.shared.domain.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body for creating or updating an org unit.
 * nature: FUNCTIONAL / NON_FUNCTIONAL; leave null to inherit from the parent.
 * locationId: optional office/location of the unit.
 */
public record OrgUnitForm(
        @NotBlank(message = "Code is required")
        @Size(max = 30, message = "Code must be at most 30 characters")
        String code,

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @NotNull(message = "Org unit type is required")
        Long typeId,

        OrgUnitNature nature,
        Long parentId,
        Long locationId,
        Integer sortOrder,
        RecordStatus status,
        Long version) {
}
