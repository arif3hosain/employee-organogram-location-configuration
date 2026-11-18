package com.bracits.pms.corehr.location.api;

/** Filters for GET /api/locations. All optional. */
public record LocationSearch(Long typeId, Long parentId, String q) {
}
