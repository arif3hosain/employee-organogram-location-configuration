package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.api.GradeForm;
import com.bracits.pms.corehr.organogram.api.GradeView;
import com.bracits.pms.corehr.organogram.service.GradeService;
import com.bracits.pms.shared.action.BaseAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveGradeAction extends BaseAction<GradeForm, GradeView> {

    private final GradeService service;

    @Override
    protected String preCondition(GradeForm f) {
        if (f == null) return "Request body is required";
        if (isBlank(f.code())) return "Code is required";
        if (tooLong(f.code(), 30)) return "Code must be at most 30 characters";
        if (isBlank(f.name())) return "Name is required";
        if (tooLong(f.name(), 100)) return "Name must be at most 100 characters";
        if (f.rankNo() == null || f.rankNo() < 1) return "Rank must be 1 or more (1 = most senior)";
        if (f.id() != null && !service.exists(f.id())) return "Grade not found: " + f.id();
        if (service.codeExists(f.code(), f.id())) return "Grade code already exists: " + f.code();
        if (service.rankExists(f.rankNo(), f.id())) return "Another grade already has rank " + f.rankNo();
        return null;
    }

    @Override
    protected GradeView execute(GradeForm f) {
        return service.save(f);
    }

    @Override
    protected String successMessage(GradeView v) {
        return "Grade saved";
    }
}
