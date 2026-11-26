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
 * A country (reference data, ISO 3166). Each country defines its own location levels
 * (LocationType.country), e.g. Bangladesh: Division > District > Upazila > Branch / Office.
 * Locations get their country through their type, so the country is stored only here.
 */
@SharedAcrossTenants
@Entity
@Table(name = "country")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Country extends BaseEntity {

    /** ISO 3166-1 alpha-2, e.g. BD. */
    @Column(nullable = false, unique = true, length = 2)
    private String iso2;

    /** ISO 3166-1 alpha-3, e.g. BGD. */
    @Column(nullable = false, unique = true, length = 3)
    private String iso3;

    @Column(nullable = false, length = 100)
    private String name;

    /** ISO 4217, e.g. BDT. */
    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    /** International dialling code, e.g. +880. */
    @Column(name = "phone_code", length = 8)
    private String phoneCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    public Country(String iso2, String iso3, String name, String currencyCode, String phoneCode) {
        change(iso2, iso3, name, currencyCode, phoneCode, RecordStatus.ACTIVE);
    }

    public void change(String iso2, String iso3, String name, String currencyCode, String phoneCode, RecordStatus status) {
        this.iso2 = iso2.trim().toUpperCase();
        this.iso3 = iso3.trim().toUpperCase();
        this.name = name.trim();
        this.currencyCode = currencyCode == null || currencyCode.isBlank() ? null : currencyCode.trim().toUpperCase();
        this.phoneCode = phoneCode == null || phoneCode.isBlank() ? null : phoneCode.trim();
        this.status = status == null ? RecordStatus.ACTIVE : status;
    }
}
