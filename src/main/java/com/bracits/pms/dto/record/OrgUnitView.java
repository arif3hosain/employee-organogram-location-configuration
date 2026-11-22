package com.bracits.pms.dto.record;

import com.bracits.pms.domain.OrgUnit;
import com.bracits.pms.domain.OrgUnitNature;
import com.bracits.pms.domain.RecordStatus;

public record OrgUnitView(Long id, String code, String name,
                          Long typeId, String typeCode, String typeName, Integer levelNo,
                          OrgUnitNature nature,
                          Long parentId, String parentName,
                          Long locationId, String locationName,
                          Integer sortOrder, String path, RecordStatus status, Long version) {

    /** Must be called inside a transaction (type, parent and location are lazy). */
    public static OrgUnitView from(OrgUnit u) {
        var p = u.getParent();
        var l = u.getLocation();
        return new OrgUnitView(u.getId(), u.getCode(), u.getName(),
                u.getType().getId(), u.getType().getCode(), u.getType().getName(), u.getType().getLevelNo(),
                u.getNature(),
                p == null ? null : p.getId(), p == null ? null : p.getName(),
                l == null ? null : l.getId(), l == null ? null : l.getName(),
                u.getSortOrder(), u.getPath(), u.getStatus(), u.getVersion());
    }
}
