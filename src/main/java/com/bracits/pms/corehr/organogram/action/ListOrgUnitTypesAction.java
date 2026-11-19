package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.api.OrgUnitTypeView;
import com.bracits.pms.corehr.organogram.service.OrgUnitTypeService;
import com.bracits.pms.shared.action.BaseAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListOrgUnitTypesAction extends BaseAction<Void, List<OrgUnitTypeView>> {

    private final OrgUnitTypeService service;

    @Override
    protected List<OrgUnitTypeView> execute(Void input) {
        return service.list();
    }
}
