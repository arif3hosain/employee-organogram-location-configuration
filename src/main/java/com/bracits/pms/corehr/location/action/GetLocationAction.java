package com.bracits.pms.corehr.location.action;

import com.bracits.pms.corehr.location.api.LocationView;
import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.shared.action.BaseAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetLocationAction extends BaseAction<Long, LocationView> {

    private final LocationService service;

    @Override
    protected LocationView execute(Long id) {
        return service.get(id);      // NotFoundException -> 404
    }
}
