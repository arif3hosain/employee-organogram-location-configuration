package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.api.OrgUnitPostView;
import com.bracits.pms.corehr.organogram.service.OrgUnitPostService;
import com.bracits.pms.corehr.organogram.service.OrgUnitService;
import com.bracits.pms.shared.action.BaseAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Input: org unit id. */
@Component
@RequiredArgsConstructor
public class ListOrgUnitPostsAction extends BaseAction<Long, List<OrgUnitPostView>> {

    private final OrgUnitPostService service;
    private final OrgUnitService orgUnitService;

    @Override
    protected String preCondition(Long orgUnitId) {
        if (!orgUnitService.exists(orgUnitId)) return "Org unit not found: " + orgUnitId;
        return null;
    }

    @Override
    protected List<OrgUnitPostView> execute(Long orgUnitId) {
        return service.listByOrgUnit(orgUnitId);
    }
}
