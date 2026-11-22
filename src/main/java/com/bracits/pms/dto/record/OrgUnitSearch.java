package com.bracits.pms.dto.record;

import com.bracits.pms.domain.OrgUnitNature;

/** Filters for GET /api/org-units. All optional. */
public record OrgUnitSearch(Long typeId, Long parentId, OrgUnitNature nature, String q) {
}
