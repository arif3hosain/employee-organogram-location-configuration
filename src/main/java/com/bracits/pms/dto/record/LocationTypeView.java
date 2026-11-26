package com.bracits.pms.dto.record;

import com.bracits.pms.domain.LocationType;
import com.bracits.pms.domain.RecordStatus;

public record LocationTypeView(Long id, Long countryId, String countryCode, String countryName,
                               String code, String name, Integer levelNo, RecordStatus status, Long version) {

    /** Must be called inside a transaction (country is lazy). */
    public static LocationTypeView from(LocationType t) {
        var c = t.getCountry();
        return new LocationTypeView(t.getId(), c.getId(), c.getIso2(), c.getName(),
                t.getCode(), t.getName(), t.getLevelNo(), t.getStatus(), t.getVersion());
    }
}
