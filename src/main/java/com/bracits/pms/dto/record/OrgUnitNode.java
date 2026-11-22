package com.bracits.pms.dto.record;

import com.bracits.pms.domain.OrgUnitNature;
import com.bracits.pms.domain.RecordStatus;
import java.util.ArrayList;
import java.util.List;

/** Organogram chart node returned by GET /api/org-units/tree. */
public record OrgUnitNode(Long id, String code, String name, String typeCode, String typeName, Integer levelNo,
                          OrgUnitNature nature, String locationName, RecordStatus status,
                          List<OrgUnitNode> children) {

    public static OrgUnitNode of(OrgUnitView v) {
        return new OrgUnitNode(v.id(), v.code(), v.name(), v.typeCode(), v.typeName(), v.levelNo(),
                v.nature(), v.locationName(), v.status(), new ArrayList<>());
    }
}
