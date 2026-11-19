package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.api.OrgUnitTypeForm;
import com.bracits.pms.corehr.organogram.api.OrgUnitTypeView;
import com.bracits.pms.corehr.organogram.service.OrgUnitTypeService;
import com.bracits.pms.shared.action.BaseAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveOrgUnitTypeAction extends BaseAction<OrgUnitTypeForm, OrgUnitTypeView> {

    private final OrgUnitTypeService service;

    @Override
    protected String preCondition(OrgUnitTypeForm f) {
        if (f == null) return "Request body is required";
        if (isBlank(f.code())) return "Code is required";
        if (tooLong(f.code(), 30)) return "Code must be at most 30 characters";
        if (isBlank(f.name())) return "Name is required";
        if (tooLong(f.name(), 100)) return "Name must be at most 100 characters";
        if (f.levelNo() == null || f.levelNo() < 1) return "Level number must be 1 or more (1 = top)";
        if (f.id() != null && !service.exists(f.id())) return "Org unit type not found: " + f.id();
        if (service.codeExists(f.code(), f.id())) return "Org unit type code already exists: " + f.code();
        return null;
    }

    @Override
    protected OrgUnitTypeView execute(OrgUnitTypeForm f) {
        return service.save(f);
    }

    @Override
    protected String successMessage(OrgUnitTypeView v) {
        return "Org unit type saved";
    }
}
