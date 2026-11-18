package com.bracits.pms.corehr.organogram.api;

import com.bracits.pms.shared.domain.RecordStatus;

/** Create (id null) or update (id set) a designation. The grade is picked from the Grade list. */
public record DesignationForm(Long id, String code, String name, Long gradeId, RecordStatus status, Long version) {
}
