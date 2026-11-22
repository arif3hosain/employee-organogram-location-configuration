package com.bracits.pms.dto.record;

import com.bracits.pms.domain.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Body for creating or updating a location. version should be sent on update to detect stale edits. */
public record LocationForm(
        @NotBlank(message = "Code is required")
        @Size(max = 30, message = "Code must be at most 30 characters")
        String code,

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @NotNull(message = "Location type is required")
        Long typeId,

        Long parentId,
        RecordStatus status,
        Long version) {
}
