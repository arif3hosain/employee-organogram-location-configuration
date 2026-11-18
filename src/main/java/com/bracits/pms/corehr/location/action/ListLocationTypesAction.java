package com.bracits.pms.corehr.location.action;

import com.bracits.pms.corehr.location.api.LocationTypeView;
import com.bracits.pms.corehr.location.service.LocationTypeService;
import com.bracits.pms.shared.action.BaseAction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListLocationTypesAction extends BaseAction<Void, List<LocationTypeView>> {

    private final LocationTypeService service;

    public ListLocationTypesAction(LocationTypeService service) {
        this.service = service;
    }

    @Override
    protected List<LocationTypeView> execute(Void input) {
        return service.list();
    }
}
