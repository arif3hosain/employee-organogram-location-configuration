package com.bracits.pms.corehr.location.api;

import com.bracits.pms.shared.domain.RecordStatus;

/** Create (id null) or update (id set) a location type. */
public record LocationTypeForm(Long id, String code, String name, Integer levelNo, RecordStatus status, Long version) {
}
