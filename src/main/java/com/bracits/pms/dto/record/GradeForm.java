package com.bracits.pms.dto.record;

import com.bracits.pms.domain.RecordStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Body for creating or updating a grade (the id comes from the URL on update). rankNo 1 = most senior, unique. */
public record GradeForm(
        @NotBlank(message = "Code is required")
        @Size(max = 30, message = "Code must be at most 30 characters")
        String code,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotNull(message = "Rank must be 1 or more (1 = most senior)")
        @Min(value = 1, message = "Rank must be 1 or more (1 = most senior)")
        Integer rankNo,

        RecordStatus status,
        Long version) {
}
