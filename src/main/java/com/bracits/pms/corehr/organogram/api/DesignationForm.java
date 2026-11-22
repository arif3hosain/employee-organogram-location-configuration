package com.bracits.pms.corehr.organogram.api;

import com.bracits.pms.shared.domain.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Body for creating or updating a designation (the id comes from the URL on update). The grade is picked from the Grade list. */
public record DesignationForm(
        @NotBlank(message = "Code is required")
        @Size(max = 30, message = "Code must be at most 30 characters")
        String code,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotNull(message = "Grade is required")
        Long gradeId,

        RecordStatus status,
        Long version) {
}
