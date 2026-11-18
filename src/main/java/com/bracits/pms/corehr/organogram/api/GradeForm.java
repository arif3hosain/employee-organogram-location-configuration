package com.bracits.pms.corehr.organogram.api;

import com.bracits.pms.shared.domain.RecordStatus;

/** Create (id null) or update (id set) a grade. rankNo 1 = most senior, unique. */
public record GradeForm(Long id, String code, String name, Integer rankNo, RecordStatus status, Long version) {
}
