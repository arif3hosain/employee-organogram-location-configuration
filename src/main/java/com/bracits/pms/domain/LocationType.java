package com.bracits.pms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A configurable kind of location, defined per country. Seeded for Bangladesh as
 * DIVISION (1) > DISTRICT (2) > UPAZILA (3) > BRANCH / OFFICE (4); India could define
 * STATE > DISTRICT > TEHSIL > BLOCK > VILLAGE without any schema change.
 * The country is stored here only: every location gets its country through its type.
 * levelNo 1 = top (a root of the tree). A child's type must have a larger levelNo than its parent's type.
 * Codes are unique per country (two countries may both have a DISTRICT type).
 */
@Entity
@Table(name = "location_type",
        uniqueConstraints = @UniqueConstraint(name = "uk_location_type_country_code", columnNames = {"tenant_id", "country_id", "code"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationType extends TenantScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "level_no", nullable = false)
    private Integer levelNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    public LocationType(Country country, String code, String name, Integer levelNo) {
        this.country = country;
        change(code, name, levelNo, RecordStatus.ACTIVE);
    }

    /** Move an unused type to another country (the service checks that no location uses it). */
    public void moveToCountry(Country country) {
        this.country = country;
    }

    public void change(String code, String name, Integer levelNo, RecordStatus status) {
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.levelNo = levelNo;
        this.status = status == null ? RecordStatus.ACTIVE : status;
    }
}
