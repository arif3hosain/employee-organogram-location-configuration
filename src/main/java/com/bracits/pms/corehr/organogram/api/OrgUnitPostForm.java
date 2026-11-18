package com.bracits.pms.corehr.organogram.api;

/** Set the sanctioned number of posts for a designation in an org unit (creates or updates). */
public record OrgUnitPostForm(Long orgUnitId, Long designationId, Integer sanctionedCount) {
}
