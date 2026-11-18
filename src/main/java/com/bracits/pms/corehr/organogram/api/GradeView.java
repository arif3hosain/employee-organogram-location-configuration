package com.bracits.pms.corehr.organogram.api;

import com.bracits.pms.corehr.organogram.domain.Grade;
import com.bracits.pms.shared.domain.RecordStatus;

public record GradeView(Long id, String code, String name, Integer rankNo, RecordStatus status, Long version) {

    public static GradeView from(Grade g) {
        return new GradeView(g.getId(), g.getCode(), g.getName(), g.getRankNo(), g.getStatus(), g.getVersion());
    }
}
