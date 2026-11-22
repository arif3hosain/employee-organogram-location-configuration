package com.bracits.pms.dto.record;

/** Filters for GET /api/locations. All optional. */
public record LocationSearch(Long typeId, Long parentId, String q) {
}
