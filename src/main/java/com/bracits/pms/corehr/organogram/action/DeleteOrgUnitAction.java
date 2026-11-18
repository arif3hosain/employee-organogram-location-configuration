package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.service.OrgUnitService;
import com.bracits.pms.shared.action.BaseAction;
import org.springframework.stereotype.Component;

@Component
public class DeleteOrgUnitAction extends BaseAction<Long, Long> {

    private final OrgUnitService service;

    public DeleteOrgUnitAction(OrgUnitService service) {
        this.service = service;
    }

    @Override
    protected String preCondition(Long id) {
        if (id == null) return "Id is required";
        if (!service.exists(id)) return "Org unit not found: " + id;
        return null;
    }

    @Override
    protected Long execute(Long id) {
        service.delete(id);
        return id;
    }

    @Override
    protected String successMessage(Long id) {
        return "Org unit deleted";
    }
}
