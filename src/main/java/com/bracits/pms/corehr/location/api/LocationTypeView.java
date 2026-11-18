package com.bracits.pms.corehr.location.api;

import com.bracits.pms.corehr.location.domain.LocationType;
import com.bracits.pms.shared.domain.RecordStatus;

public record LocationTypeView(Long id, String code, String name, Integer levelNo, RecordStatus status, Long version) {

    public static LocationTypeView from(LocationType t) {
        return new LocationTypeView(t.getId(), t.getCode(), t.getName(), t.getLevelNo(), t.getStatus(), t.getVersion());
    }
}
