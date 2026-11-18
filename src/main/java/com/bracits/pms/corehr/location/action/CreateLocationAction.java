package com.bracits.pms.corehr.location.action;

import com.bracits.pms.corehr.location.api.LocationForm;
import com.bracits.pms.corehr.location.api.LocationView;
import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.corehr.location.service.LocationTypeService;
import com.bracits.pms.shared.action.BaseAction;
import org.springframework.stereotype.Component;

@Component
public class CreateLocationAction extends BaseAction<LocationForm, LocationView> {

    private final LocationService service;
    private final LocationTypeService typeService;

    public CreateLocationAction(LocationService service, LocationTypeService typeService) {
        this.service = service;
        this.typeService = typeService;
    }

    @Override
    protected String preCondition(LocationForm f) {
        String error = LocationFormRules.check(f);
        if (error != null) return error;
        if (!typeService.exists(f.typeId())) return "Location type not found: " + f.typeId();
        if (f.parentId() != null && !service.exists(f.parentId())) return "Parent location not found: " + f.parentId();
        if (service.codeExists(f.code(), null)) return "Location code already exists: " + f.code();
        return null;   // level rule is checked in the service, where the types are loaded
    }

    @Override
    protected LocationView execute(LocationForm f) {
        return service.create(f);
    }

    @Override
    protected String successMessage(LocationView v) {
        return "Location " + v.code() + " created";
    }
}
