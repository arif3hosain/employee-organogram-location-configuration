package com.bracits.pms.domain;

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

/**
 * One node of the location tree. Every node has a type; the tree is a single self-referencing
 * table, so new levels (Zone, Cluster, ...) need no schema change.
 * path = "/rootId/.../ownId/" for fast subtree queries and cycle checks.
 */
@Entity
@Table(name = "location")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location extends TenantScopedEntity {

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_type_id", nullable = false)
    private LocationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Location parent;

    @Column(length = 1000)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    public Location(String code, String name, LocationType type, Location parent) {
        change(code, name, type, parent, RecordStatus.ACTIVE);
    }

    public void change(String code, String name, LocationType type, Location parent, RecordStatus status) {
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.type = type;
        this.parent = parent;
        this.status = status == null ? RecordStatus.ACTIVE : status;
    }

    /** A location's country is its type's country (not stored on the location). */
    public Country getCountry() {
        return type.getCountry();
    }

    /** Call once the id is assigned (after the first save), and again after a move. */
    public void refreshPath() {
        this.path = HierarchyPath.childPath(parent == null ? null : parent.getPath(), getId());
    }

    public void rebasePath(String oldPrefix, String newPrefix) {
        this.path = HierarchyPath.rebase(path, oldPrefix, newPrefix);
    }
}
