package com.bracits.pms.corehr.employee.api;

import com.bracits.pms.shared.domain.RecordStatus;

import java.time.LocalDate;

/** Body for creating or updating an employee. version is required on update to detect stale edits. */
public record EmployeeForm(String employeeCode, String fullName, String email, String phone, LocalDate joinDate,
                           Long designationId, Long orgUnitId, Long locationId, Long supervisorId,
                           RecordStatus status, Long version) {
}
