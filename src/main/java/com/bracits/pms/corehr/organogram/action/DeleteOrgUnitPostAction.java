package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.service.OrgUnitPostService;
import com.bracits.pms.shared.action.BaseAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteOrgUnitPostAction extends BaseAction<Long, Long> {

    private final OrgUnitPostService service;

    @Override
    protected String preCondition(Long id) {
        if (id == null) return "Id is required";
        if (!service.exists(id)) return "Org unit post not found: " + id;
        return null;
    }

    @Override
    protected Long execute(Long id) {
        service.delete(id);
        return id;
    }

    @Override
    protected String successMessage(Long id) {
        return "Sanctioned post line deleted";
    }
}
