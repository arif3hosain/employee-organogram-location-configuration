package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.api.OrgUnitTypeView;
import com.bracits.pms.corehr.organogram.service.OrgUnitTypeService;
import com.bracits.pms.shared.action.BaseAction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListOrgUnitTypesAction extends BaseAction<Void, List<OrgUnitTypeView>> {

    private final OrgUnitTypeService service;

    public ListOrgUnitTypesAction(OrgUnitTypeService service) {
        this.service = service;
    }

    @Override
    protected List<OrgUnitTypeView> execute(Void input) {
        return service.list();
    }
}
