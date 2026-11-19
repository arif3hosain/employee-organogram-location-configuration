package com.bracits.pms.corehr.organogram.domain;

import com.bracits.pms.shared.domain.BaseEntity;
import com.bracits.pms.shared.domain.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Job title. Its seniority comes from its Grade (no hand-typed rank). */
@Entity
@Table(name = "designation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Designation extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    public Designation(String code, String name, Grade grade) {
        change(code, name, grade, RecordStatus.ACTIVE);
    }

    public void change(String code, String name, Grade grade, RecordStatus status) {
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.grade = grade;
        this.status = status == null ? RecordStatus.ACTIVE : status;
    }
}
