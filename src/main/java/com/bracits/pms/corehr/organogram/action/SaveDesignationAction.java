package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.api.DesignationForm;
import com.bracits.pms.corehr.organogram.api.DesignationView;
import com.bracits.pms.corehr.organogram.service.DesignationService;
import com.bracits.pms.corehr.organogram.service.GradeService;
import com.bracits.pms.shared.action.BaseAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveDesignationAction extends BaseAction<DesignationForm, DesignationView> {

    private final DesignationService service;
    private final GradeService gradeService;

    @Override
    protected String preCondition(DesignationForm f) {
        if (f == null) return "Request body is required";
        if (isBlank(f.code())) return "Code is required";
        if (tooLong(f.code(), 30)) return "Code must be at most 30 characters";
        if (isBlank(f.name())) return "Name is required";
        if (tooLong(f.name(), 100)) return "Name must be at most 100 characters";
        if (f.gradeId() == null) return "Grade is required";
        if (!gradeService.exists(f.gradeId())) return "Grade not found: " + f.gradeId();
        if (f.id() != null && !service.exists(f.id())) return "Designation not found: " + f.id();
        if (service.codeExists(f.code(), f.id())) return "Designation code already exists: " + f.code();
        return null;   // "grade must be ACTIVE" is checked in the service, where the grade is loaded
    }

    @Override
    protected DesignationView execute(DesignationForm f) {
        return service.save(f);
    }

    @Override
    protected String successMessage(DesignationView v) {
        return "Designation saved";
    }
}
