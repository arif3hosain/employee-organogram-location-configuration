package com.bracits.pms.dto.record;

import com.bracits.pms.domain.RecordStatus;
import java.util.ArrayList;
import java.util.List;

/** Tree node returned by GET /api/locations/tree. */
public record LocationNode(Long id, String code, String name, Long countryId, String countryCode, String countryName,
                           String typeCode, String typeName, Integer levelNo, RecordStatus status,
                           List<LocationNode> children) {

    public static LocationNode of(LocationView v) {
        return new LocationNode(v.id(), v.code(), v.name(), v.countryId(), v.countryCode(), v.countryName(),
                v.typeCode(), v.typeName(), v.levelNo(), v.status(), new ArrayList<>());
    }
}
