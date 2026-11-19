package com.bracits.pms.corehr.location.action;

import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.shared.action.BaseAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteLocationAction extends BaseAction<Long, Long> {

    private final LocationService service;

    @Override
    protected String preCondition(Long id) {
        if (id == null) return "Id is required";
        if (!service.exists(id)) return "Location not found: " + id;
        return null;
    }

    @Override
    protected Long execute(Long id) {
        service.delete(id);
        return id;
    }

    @Override
    protected String successMessage(Long id) {
        return "Location deleted";
    }
}
