package com.bracits.pms.dto.record;

import com.bracits.pms.domain.Designation;
import com.bracits.pms.domain.RecordStatus;

/** gradeRank is the grade's rankNo, kept for sorting and display. */
public record DesignationView(Long id, String code, String name,
                              Long gradeId, String gradeCode, String gradeName, Integer gradeRank,
                              RecordStatus status, Long version) {

    /** Must be called inside a transaction (grade is lazy). */
    public static DesignationView from(Designation d) {
        var g = d.getGrade();
        return new DesignationView(d.getId(), d.getCode(), d.getName(),
                g.getId(), g.getCode(), g.getName(), g.getRankNo(), d.getStatus(), d.getVersion());
    }
}
