package com.bracits.pms.dto.record;

import com.bracits.pms.domain.Location;
import com.bracits.pms.domain.RecordStatus;

public record LocationView(Long id, String code, String name,
                           Long countryId, String countryCode, String countryName,
                           Long typeId, String typeCode, String typeName, Integer levelNo,
                           Long parentId, String parentName, String path,
                           RecordStatus status, Long version) {

    /** Must be called inside a transaction (type, its country and parent are lazy). */
    public static LocationView from(Location l) {
        var p = l.getParent();
        var c = l.getCountry();
        return new LocationView(l.getId(), l.getCode(), l.getName(),
                c.getId(), c.getIso2(), c.getName(),
                l.getType().getId(), l.getType().getCode(), l.getType().getName(), l.getType().getLevelNo(),
                p == null ? null : p.getId(), p == null ? null : p.getName(), l.getPath(),
                l.getStatus(), l.getVersion());
    }
}