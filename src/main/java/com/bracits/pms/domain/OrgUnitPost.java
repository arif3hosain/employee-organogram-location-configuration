package com.bracits.pms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Sanctioned (approved) posts: how many people of a designation an org unit may have.
 * Organogram = org unit tree + sanctioned posts per unit. Vacancy = sanctioned - active employees.
 */
@Entity
@Table(name = "org_unit_post",
        uniqueConstraints = @UniqueConstraint(columnNames = {"org_unit_id", "designation_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrgUnitPost extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_unit_id", nullable = false)
    private OrgUnit orgUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "designation_id", nullable = false)
    private Designation designation;

    @Column(name = "sanctioned_count", nullable = false)
    private Integer sanctionedCount;

    public OrgUnitPost(OrgUnit orgUnit, Designation designation, Integer sanctionedCount) {
        this.orgUnit = orgUnit;
        this.designation = designation;
        this.sanctionedCount = sanctionedCount;
    }

    public void changeSanctionedCount(Integer sanctionedCount) {
        this.sanctionedCount = sanctionedCount;
    }
}
