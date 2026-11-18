package com.bracits.pms.corehr.organogram.api;

import com.bracits.pms.corehr.organogram.domain.OrgUnitType;
import com.bracits.pms.shared.domain.RecordStatus;

public record OrgUnitTypeView(Long id, String code, String name, Integer levelNo, RecordStatus status, Long version) {

    public static OrgUnitTypeView from(OrgUnitType t) {
        return new OrgUnitTypeView(t.getId(), t.getCode(), t.getName(), t.getLevelNo(), t.getStatus(), t.getVersion());
    }
}
