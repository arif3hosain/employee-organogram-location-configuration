package com.bracits.pms.corehr.employee.action;

import com.bracits.pms.corehr.employee.api.EmployeeForm;
import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.corehr.organogram.service.DesignationService;
import com.bracits.pms.corehr.organogram.service.OrgUnitService;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/** preCondition checks shared by CreateEmployeeAction and UpdateEmployeeAction. */
@Component
class EmployeeFormRules {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^[+0-9][0-9 -]{5,29}$");

    private final DesignationService designationService;
    private final OrgUnitService orgUnitService;
    private final LocationService locationService;

    EmployeeFormRules(DesignationService designationService, OrgUnitService orgUnitService,
                      LocationService locationService) {
        this.designationService = designationService;
        this.orgUnitService = orgUnitService;
        this.locationService = locationService;
    }

    /** Field format checks, then "does the referenced record exist" checks. */
    String check(EmployeeForm f) {
        if (f == null) return "Request body is required";
        if (blank(f.employeeCode())) return "Employee code is required";
        if (f.employeeCode().length() > 30) return "Employee code must be at most 30 characters";
        if (blank(f.fullName())) return "Full name is required";
        if (f.fullName().length() > 150) return "Full name must be at most 150 characters";
        if (blank(f.email())) return "Email is required";
        if (f.email().length() > 150 || !EMAIL.matcher(f.email().trim()).matches()) return "Email is not valid";
        if (!blank(f.phone()) && !PHONE.matcher(f.phone().trim()).matches()) return "Phone is not valid";
        if (f.joinDate() == null) return "Join date is required";
        if (f.designationId() == null) return "Designation is required";
        if (f.orgUnitId() == null) return "Org unit is required";
        if (f.locationId() == null) return "Location is required";

        if (!designationService.exists(f.designationId())) return "Designation not found: " + f.designationId();
        if (!orgUnitService.exists(f.orgUnitId())) return "Org unit not found: " + f.orgUnitId();
        if (!locationService.exists(f.locationId())) return "Location not found: " + f.locationId();
        return null;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
