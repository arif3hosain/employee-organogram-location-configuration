package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.api.OrgUnitNode;
import com.bracits.pms.corehr.organogram.service.OrgUnitService;
import com.bracits.pms.shared.action.BaseAction;
import org.springframework.stereotype.Component;

import java.util.List;

/** Input: optional root unit id (null = whole organogram). */
@Component
public class GetOrganogramTreeAction extends BaseAction<Long, List<OrgUnitNode>> {

    private final OrgUnitService service;

    public GetOrganogramTreeAction(OrgUnitService service) {
        this.service = service;
    }

    @Override
    protected List<OrgUnitNode> execute(Long rootId) {
        return service.tree(rootId);
    }
}
