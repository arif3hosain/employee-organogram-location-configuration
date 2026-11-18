package com.bracits.pms.corehr.location.action;

import com.bracits.pms.corehr.location.api.LocationSearch;
import com.bracits.pms.corehr.location.api.LocationView;
import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.shared.action.BaseAction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchLocationsAction extends BaseAction<LocationSearch, List<LocationView>> {

    private final LocationService service;

    public SearchLocationsAction(LocationService service) {
        this.service = service;
    }

    @Override
    protected List<LocationView> execute(LocationSearch search) {
        return service.search(search);
    }
}
