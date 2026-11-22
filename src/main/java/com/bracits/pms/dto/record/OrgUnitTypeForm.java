package com.bracits.pms.dto.record;

import com.bracits.pms.domain.RecordStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Body for creating or updating a type (the id comes from the URL on update). levelNo 1 = top. */
public record OrgUnitTypeForm(
        @NotBlank(message = "Code is required")
        @Size(max = 30, message = "Code must be at most 30 characters")
        String code,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotNull(message = "Level number must be 1 or more (1 = top)")
        @Min(value = 1, message = "Level number must be 1 or more (1 = top)")
        Integer levelNo,

        RecordStatus status,
        Long version) {
}
