package com.bracits.pms.corehr.location.action;

import com.bracits.pms.corehr.location.api.LocationView;
import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.shared.action.BaseAction;
import org.springframework.stereotype.Component;

@Component
public class GetLocationAction extends BaseAction<Long, LocationView> {

    private final LocationService service;

    public GetLocationAction(LocationService service) {
        this.service = service;
    }

    @Override
    protected LocationView execute(Long id) {
        return service.get(id);      // NotFoundException -> 404
    }
}
