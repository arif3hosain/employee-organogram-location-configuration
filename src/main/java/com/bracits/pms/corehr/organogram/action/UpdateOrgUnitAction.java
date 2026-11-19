package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.corehr.organogram.api.OrgUnitForm;
import com.bracits.pms.corehr.organogram.api.OrgUnitView;
import com.bracits.pms.corehr.organogram.service.OrgUnitService;
import com.bracits.pms.corehr.organogram.service.OrgUnitTypeService;
import com.bracits.pms.shared.action.BaseAction;
import com.bracits.pms.shared.action.UpdateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateOrgUnitAction extends BaseAction<UpdateCommand<OrgUnitForm>, OrgUnitView> {

    private final OrgUnitService service;
    private final OrgUnitTypeService typeService;
    private final LocationService locationService;

    @Override
    protected String preCondition(UpdateCommand<OrgUnitForm> c) {
        String error = OrgUnitFormRules.check(c.form());
        if (error != null) return error;
        OrgUnitForm f = c.form();
        if (!service.exists(c.id())) return "Org unit not found: " + c.id();
        if (c.id().equals(f.parentId())) return "An org unit cannot be its own parent";
        if (!typeService.exists(f.typeId())) return "Org unit type not found: " + f.typeId();
        if (f.parentId() != null && !service.exists(f.parentId())) return "Parent org unit not found: " + f.parentId();
        if (f.locationId() != null && !locationService.exists(f.locationId())) return "Location not found: " + f.locationId();
        if (service.codeExists(f.code(), c.id())) return "Org unit code already exists: " + f.code();
        return null;
    }

    @Override
    protected OrgUnitView execute(UpdateCommand<OrgUnitForm> c) {
        return service.update(c.id(), c.form());
    }

    @Override
    protected String successMessage(OrgUnitView v) {
        return "Org unit " + v.code() + " updated";
    }
}
