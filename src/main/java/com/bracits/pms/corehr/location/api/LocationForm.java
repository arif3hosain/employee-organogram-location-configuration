package com.bracits.pms.corehr.location.api;

import com.bracits.pms.shared.domain.RecordStatus;

/** Body for creating or updating a location. version is required on update to detect stale edits. */
public record LocationForm(String code, String name, Long typeId, Long parentId, RecordStatus status, Long version) {
}
