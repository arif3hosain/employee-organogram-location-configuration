package com.bracits.pms.corehr.location.action;

import com.bracits.pms.corehr.location.api.LocationNode;
import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.shared.action.BaseAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Input: optional root id (null = whole tree). */
@Component
@RequiredArgsConstructor
public class GetLocationTreeAction extends BaseAction<Long, List<LocationNode>> {

    private final LocationService service;

    @Override
    protected List<LocationNode> execute(Long rootId) {
        return service.tree(rootId);
    }
}
