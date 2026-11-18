package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.api.OrgUnitSearch;
import com.bracits.pms.corehr.organogram.api.OrgUnitView;
import com.bracits.pms.corehr.organogram.service.OrgUnitService;
import com.bracits.pms.shared.action.BaseAction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchOrgUnitsAction extends BaseAction<OrgUnitSearch, List<OrgUnitView>> {

    private final OrgUnitService service;

    public SearchOrgUnitsAction(OrgUnitService service) {
        this.service = service;
    }

    @Override
    protected List<OrgUnitView> execute(OrgUnitSearch search) {
        return service.search(search);
    }
}
