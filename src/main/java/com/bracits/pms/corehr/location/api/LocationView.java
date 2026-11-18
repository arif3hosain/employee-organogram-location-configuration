package com.bracits.pms.corehr.location.api;

import com.bracits.pms.corehr.location.domain.Location;
import com.bracits.pms.shared.domain.RecordStatus;

public record LocationView(Long id, String code, String name,
                           Long typeId, String typeCode, String typeName, Integer levelNo,
                           Long parentId, String parentName, String path,
                           RecordStatus status, Long version) {

    /** Must be called inside a transaction (type and parent are lazy). */
    public static LocationView from(Location l) {
        var p = l.getParent();
        return new LocationView(l.getId(), l.getCode(), l.getName(),
                l.getType().getId(), l.getType().getCode(), l.getType().getName(), l.getType().getLevelNo(),
                p == null ? null : p.getId(), p == null ? null : p.getName(), l.getPath(),
                l.getStatus(), l.getVersion());
    }
}
