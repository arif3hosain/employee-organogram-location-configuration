package com.bracits.pms.corehr.organogram.api;

import com.bracits.pms.shared.domain.RecordStatus;

/** Create (id null) or update (id set) an org unit type. */
public record OrgUnitTypeForm(Long id, String code, String name, Integer levelNo, RecordStatus status, Long version) {
}
