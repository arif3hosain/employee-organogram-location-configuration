package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.corehr.organogram.api.OrgUnitForm;
import com.bracits.pms.corehr.organogram.api.OrgUnitView;
import com.bracits.pms.corehr.organogram.service.OrgUnitService;
import com.bracits.pms.corehr.organogram.service.OrgUnitTypeService;
import com.bracits.pms.shared.action.BaseAction;
import org.springframework.stereotype.Component;

@Component
public class CreateOrgUnitAction extends BaseAction<OrgUnitForm, OrgUnitView> {

    private final OrgUnitService service;
    private final OrgUnitTypeService typeService;
    private final LocationService locationService;

    public CreateOrgUnitAction(OrgUnitService service, OrgUnitTypeService typeService, LocationService locationService) {
        this.service = service;
        this.typeService = typeService;
        this.locationService = locationService;
    }

    @Override
    protected String preCondition(OrgUnitForm f) {
        String error = OrgUnitFormRules.check(f);
        if (error != null) return error;
        if (!typeService.exists(f.typeId())) return "Org unit type not found: " + f.typeId();
        if (f.parentId() != null && !service.exists(f.parentId())) return "Parent org unit not found: " + f.parentId();
        if (f.locationId() != null && !locationService.exists(f.locationId())) return "Location not found: " + f.locationId();
        if (service.codeExists(f.code(), null)) return "Org unit code already exists: " + f.code();
        return null;
    }

    @Override
    protected OrgUnitView execute(OrgUnitForm f) {
        return service.create(f);
    }

    @Override
    protected String successMessage(OrgUnitView v) {
        return "Org unit " + v.code() + " created";
    }
}
