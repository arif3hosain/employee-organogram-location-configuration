package com.bracits.pms.corehr.employee.api;

import com.bracits.pms.shared.domain.RecordStatus;

/**
 * Filters for GET /api/employees. All optional.
 * orgUnitId and locationId include everything below them in the tree.
 */
public record EmployeeSearch(String q, Long orgUnitId, Long locationId, Long designationId, RecordStatus status,
                             int page, int size) {
}
