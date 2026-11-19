package com.bracits.pms.corehr.location.domain;

import com.bracits.pms.shared.domain.BaseEntity;
import com.bracits.pms.shared.domain.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A configurable kind of location. Seeded for Bangladesh as
 * COUNTRY (1) > DIVISION (2) > DISTRICT (3) > UPAZILA (4) > BRANCH / OFFICE (5).
 * Unlike the legacy system there are no separate Division / District / Upazila tables:
 * the levels are rows here, so another country or a new level needs no schema change.
 * levelNo 1 = top. A child's type must have a larger levelNo than its parent's type.
 */
@Entity
@Table(name = "location_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationType extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "level_no", nullable = false)
    private Integer levelNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    public LocationType(String code, String name, Integer levelNo) {
        change(code, name, levelNo, RecordStatus.ACTIVE);
    }

    public void change(String code, String name, Integer levelNo, RecordStatus status) {
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.levelNo = levelNo;
        this.status = status == null ? RecordStatus.ACTIVE : status;
    }
}
