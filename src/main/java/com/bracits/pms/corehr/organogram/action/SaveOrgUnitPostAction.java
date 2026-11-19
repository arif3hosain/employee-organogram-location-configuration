package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.api.OrgUnitPostForm;
import com.bracits.pms.corehr.organogram.api.OrgUnitPostView;
import com.bracits.pms.corehr.organogram.service.DesignationService;
import com.bracits.pms.corehr.organogram.service.OrgUnitPostService;
import com.bracits.pms.corehr.organogram.service.OrgUnitService;
import com.bracits.pms.shared.action.BaseAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveOrgUnitPostAction extends BaseAction<OrgUnitPostForm, OrgUnitPostView> {

    private final OrgUnitPostService service;
    private final OrgUnitService orgUnitService;
    private final DesignationService designationService;

    @Override
    protected String preCondition(OrgUnitPostForm f) {
        if (f == null) return "Request body is required";
        if (f.orgUnitId() == null) return "Org unit is required";
        if (f.designationId() == null) return "Designation is required";
        if (f.sanctionedCount() == null || f.sanctionedCount() < 0) return "Sanctioned count must be 0 or more";
        if (!orgUnitService.exists(f.orgUnitId())) return "Org unit not found: " + f.orgUnitId();
        if (!designationService.exists(f.designationId())) return "Designation not found: " + f.designationId();
        return null;
    }

    @Override
    protected OrgUnitPostView execute(OrgUnitPostForm f) {
        return service.save(f);
    }

    @Override
    protected void postCondition(OrgUnitPostForm f, OrgUnitPostView v) {
        if (v.vacancy() < 0) {
            log.warn("Org unit {} is over-staffed for {} by {}", v.orgUnitId(), v.designationCode(), -v.vacancy());
        }
    }

    @Override
    protected String successMessage(OrgUnitPostView v) {
        return v.vacancy() < 0
                ? "Saved. Warning: " + (-v.vacancy()) + " more active employee(s) than sanctioned posts"
                : "Sanctioned posts saved";
    }
}
