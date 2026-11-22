package com.bracits.pms.corehr.employee.api;

import com.bracits.pms.shared.api.OnUpdate;
import com.bracits.pms.shared.domain.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Body for creating or updating an employee. version is required on update to detect stale edits.
 * Whether the referenced designation / org unit / location / supervisor exist (and are ACTIVE),
 * and uniqueness of code and email, are checked in EmployeeService.
 */
public record EmployeeForm(
        @NotBlank(message = "Employee code is required")
        @Size(max = 30, message = "Employee code must be at most 30 characters")
        String employeeCode,

        @NotBlank(message = "Full name is required")
        @Size(max = 150, message = "Full name must be at most 150 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Size(max = 150, message = "Email is not valid")
        @Pattern(regexp = "^\\s*([^@\\s]+@[^@\\s]+\\.[^@\\s]+)?\\s*$", message = "Email is not valid")
        String email,

        @Pattern(regexp = "^\\s*([+0-9][0-9 -]{5,29})?\\s*$", message = "Phone is not valid")
        String phone,

        @NotNull(message = "Join date is required")
        LocalDate joinDate,

        @NotNull(message = "Designation is required")
        Long designationId,

        @NotNull(message = "Org unit is required")
        Long orgUnitId,

        @NotNull(message = "Location is required")
        Long locationId,

        Long supervisorId,

        RecordStatus status,

        @NotNull(groups = OnUpdate.class, message = "Version is required for update (send the version you loaded)")
        Long version) {
}
