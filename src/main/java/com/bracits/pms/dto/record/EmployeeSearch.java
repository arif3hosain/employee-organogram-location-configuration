package com.bracits.pms.dto.record;

import com.bracits.pms.domain.RecordStatus;

/**
 * Filters for GET /api/employees. All optional.
 * orgUnitId and locationId include everything below them in the tree.
 */
public record EmployeeSearch(String q, Long orgUnitId, Long locationId, Long designationId, RecordStatus status,
                             int page, int size) {
}
