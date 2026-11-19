package com.bracits.pms.corehr.location.action;

import com.bracits.pms.corehr.location.api.LocationForm;
import com.bracits.pms.corehr.location.api.LocationView;
import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.corehr.location.service.LocationTypeService;
import com.bracits.pms.shared.action.BaseAction;
import com.bracits.pms.shared.action.UpdateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateLocationAction extends BaseAction<UpdateCommand<LocationForm>, LocationView> {

    private final LocationService service;
    private final LocationTypeService typeService;

    @Override
    protected String preCondition(UpdateCommand<LocationForm> c) {
        String error = LocationFormRules.check(c.form());
        if (error != null) return error;
        LocationForm f = c.form();
        if (!service.exists(c.id())) return "Location not found: " + c.id();
        if (c.id().equals(f.parentId())) return "A location cannot be its own parent";
        if (!typeService.exists(f.typeId())) return "Location type not found: " + f.typeId();
        if (f.parentId() != null && !service.exists(f.parentId())) return "Parent location not found: " + f.parentId();
        if (service.codeExists(f.code(), c.id())) return "Location code already exists: " + f.code();
        return null;
    }

    @Override
    protected LocationView execute(UpdateCommand<LocationForm> c) {
        return service.update(c.id(), c.form());
    }

    @Override
    protected String successMessage(LocationView v) {
        return "Location " + v.code() + " updated";
    }
}
