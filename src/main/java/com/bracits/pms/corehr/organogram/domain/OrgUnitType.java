package com.bracits.pms.corehr.organogram.domain;

import com.bracits.pms.shared.domain.BaseEntity;
import com.bracits.pms.shared.domain.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Configurable organogram level, e.g. ORGANIZATION (1) > FUNCTION (2) > DEPARTMENT (3) > UNIT (4) > TEAM (5).
 * Same level rule as locations: a child unit's type must have a larger levelNo than its parent's.
 */
@Entity
@Table(name = "org_unit_type")
public class OrgUnitType extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "level_no", nullable = false)
    private Integer levelNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    protected OrgUnitType() {
    }

    public OrgUnitType(String code, String name, Integer levelNo) {
        change(code, name, levelNo, RecordStatus.ACTIVE);
    }

    public void change(String code, String name, Integer levelNo, RecordStatus status) {
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.levelNo = levelNo;
        this.status = status == null ? RecordStatus.ACTIVE : status;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getLevelNo() {
        return levelNo;
    }

    public RecordStatus getStatus() {
        return status;
    }
}
