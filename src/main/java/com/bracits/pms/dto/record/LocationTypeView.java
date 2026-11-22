package com.bracits.pms.dto.record;

import com.bracits.pms.domain.LocationType;
import com.bracits.pms.domain.RecordStatus;

public record LocationTypeView(Long id, String code, String name, Integer levelNo, RecordStatus status, Long version) {

    public static LocationTypeView from(LocationType t) {
        return new LocationTypeView(t.getId(), t.getCode(), t.getName(), t.getLevelNo(), t.getStatus(), t.getVersion());
    }
}
