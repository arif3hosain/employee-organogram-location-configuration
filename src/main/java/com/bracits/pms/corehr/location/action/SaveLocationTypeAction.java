package com.bracits.pms.corehr.location.action;

import com.bracits.pms.corehr.location.api.LocationTypeForm;
import com.bracits.pms.corehr.location.api.LocationTypeView;
import com.bracits.pms.corehr.location.service.LocationTypeService;
import com.bracits.pms.shared.action.BaseAction;
import org.springframework.stereotype.Component;

@Component
public class SaveLocationTypeAction extends BaseAction<LocationTypeForm, LocationTypeView> {

    private final LocationTypeService service;

    public SaveLocationTypeAction(LocationTypeService service) {
        this.service = service;
    }

    @Override
    protected String preCondition(LocationTypeForm f) {
        if (f == null) return "Request body is required";
        if (isBlank(f.code())) return "Code is required";
        if (tooLong(f.code(), 30)) return "Code must be at most 30 characters";
        if (isBlank(f.name())) return "Name is required";
        if (tooLong(f.name(), 100)) return "Name must be at most 100 characters";
        if (f.levelNo() == null || f.levelNo() < 1) return "Level number must be 1 or more (1 = top)";
        if (f.id() != null && !service.exists(f.id())) return "Location type not found: " + f.id();
        if (service.codeExists(f.code(), f.id())) return "Location type code already exists: " + f.code();
        return null;
    }

    @Override
    protected LocationTypeView execute(LocationTypeForm f) {
        return service.save(f);
    }

    @Override
    protected String successMessage(LocationTypeView v) {
        return "Location type saved";
    }
}
