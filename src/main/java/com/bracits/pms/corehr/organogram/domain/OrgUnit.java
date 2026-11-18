package com.bracits.pms.corehr.organogram.domain;

import com.bracits.pms.corehr.location.domain.Location;
import com.bracits.pms.shared.domain.BaseEntity;
import com.bracits.pms.shared.domain.HierarchyPath;
import com.bracits.pms.shared.domain.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * One box of the organogram (function, department, unit, team...).
 * nature: FUNCTIONAL / NON_FUNCTIONAL. If not given, it is inherited from the parent.
 * location: optional office/location where the unit sits.
 */
@Entity
@Table(name = "org_unit")
public class OrgUnit extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_unit_type_id", nullable = false)
    private OrgUnitType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OrgUnitNature nature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private OrgUnit parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(length = 1000)
    private String path;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    protected OrgUnit() {
    }

    public OrgUnit(String code, String name, OrgUnitType type, OrgUnitNature nature, OrgUnit parent,
                   Location location, Integer sortOrder) {
        change(code, name, type, nature, parent, location, sortOrder, RecordStatus.ACTIVE);
    }

    public void change(String code, String name, OrgUnitType type, OrgUnitNature nature, OrgUnit parent,
                       Location location, Integer sortOrder, RecordStatus status) {
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.type = type;
        this.parent = parent;
        this.nature = nature != null ? nature : (parent == null ? null : parent.getNature());
        this.location = location;
        this.sortOrder = sortOrder;
        this.status = status == null ? RecordStatus.ACTIVE : status;
    }

    public void refreshPath() {
        this.path = HierarchyPath.childPath(parent == null ? null : parent.getPath(), getId());
    }

    public void rebasePath(String oldPrefix, String newPrefix) {
        this.path = HierarchyPath.rebase(path, oldPrefix, newPrefix);
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public OrgUnitType getType() {
        return type;
    }

    public OrgUnitNature getNature() {
        return nature;
    }

    public OrgUnit getParent() {
        return parent;
    }

    public Location getLocation() {
        return location;
    }

    public String getPath() {
        return path;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public RecordStatus getStatus() {
        return status;
    }
}
