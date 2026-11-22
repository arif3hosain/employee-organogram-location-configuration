package com.bracits.pms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Pay/seniority grade (G1, G2, ...). Designations belong to a grade.
 * rankNo orders seniority: 1 = most senior; unique, so two grades cannot share a rank.
 */
@Entity
@Table(name = "grade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Grade extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "rank_no", nullable = false, unique = true)
    private Integer rankNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    public Grade(String code, String name, Integer rankNo) {
        change(code, name, rankNo, RecordStatus.ACTIVE);
    }

    public void change(String code, String name, Integer rankNo, RecordStatus status) {
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.rankNo = rankNo;
        this.status = status == null ? RecordStatus.ACTIVE : status;
    }
}
