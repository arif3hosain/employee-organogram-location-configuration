package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.api.OrgUnitView;
import com.bracits.pms.corehr.organogram.service.OrgUnitService;
import com.bracits.pms.shared.action.BaseAction;
import org.springframework.stereotype.Component;

@Component
public class GetOrgUnitAction extends BaseAction<Long, OrgUnitView> {

    private final OrgUnitService service;

    public GetOrgUnitAction(OrgUnitService service) {
        this.service = service;
    }

    @Override
    protected OrgUnitView execute(Long id) {
        return service.get(id);
    }
}
