package com.bracits.pms.corehr.organogram.api;

import com.bracits.pms.corehr.organogram.domain.OrgUnitNature;

/** Filters for GET /api/org-units. All optional. */
public record OrgUnitSearch(Long typeId, Long parentId, OrgUnitNature nature, String q) {
}
