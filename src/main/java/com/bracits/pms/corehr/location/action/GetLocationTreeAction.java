package com.bracits.pms.corehr.location.action;

import com.bracits.pms.corehr.location.api.LocationNode;
import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.shared.action.BaseAction;
import org.springframework.stereotype.Component;

import java.util.List;

/** Input: optional root id (null = whole tree). */
@Component
public class GetLocationTreeAction extends BaseAction<Long, List<LocationNode>> {

    private final LocationService service;

    public GetLocationTreeAction(LocationService service) {
        this.service = service;
    }

    @Override
    protected List<LocationNode> execute(Long rootId) {
        return service.tree(rootId);
    }
}
