package com.bracits.pms.corehr.organogram.api;

import com.bracits.pms.corehr.organogram.domain.OrgUnitNature;
import com.bracits.pms.shared.domain.RecordStatus;

/**
 * Body for creating or updating an org unit.
 * nature: FUNCTIONAL / NON_FUNCTIONAL; leave null to inherit from the parent.
 * locationId: optional office/location of the unit.
 */
public record OrgUnitForm(String code, String name, Long typeId, OrgUnitNature nature, Long parentId,
                          Long locationId, Integer sortOrder, RecordStatus status, Long version) {
}
