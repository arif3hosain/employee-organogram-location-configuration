package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.service.OrgUnitTypeService;
import com.bracits.pms.shared.action.BaseAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteOrgUnitTypeAction extends BaseAction<Long, Long> {

    private final OrgUnitTypeService service;

    @Override
    protected String preCondition(Long id) {
        if (id == null) return "Id is required";
        if (!service.exists(id)) return "Org unit type not found: " + id;
        return null;
    }

    @Override
    protected Long execute(Long id) {
        service.delete(id);
        return id;
    }

    @Override
    protected String successMessage(Long id) {
        return "Org unit type deleted";
    }
}
