package com.bracits.pms.corehr.organogram.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Set the sanctioned number of posts for a designation in an org unit (creates or updates). The unit id comes from the URL. */
public record OrgUnitPostForm(
        @NotNull(message = "Designation is required")
        Long designationId,

        @NotNull(message = "Sanctioned count must be 0 or more")
        @Min(value = 0, message = "Sanctioned count must be 0 or more")
        Integer sanctionedCount) {
}
