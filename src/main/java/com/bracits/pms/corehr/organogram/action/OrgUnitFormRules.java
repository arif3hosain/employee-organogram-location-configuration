package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.api.OrgUnitForm;

/** Field checks shared by CreateOrgUnitAction and UpdateOrgUnitAction. */
final class OrgUnitFormRules {

    private OrgUnitFormRules() {
    }

    static String check(OrgUnitForm f) {
        if (f == null) return "Request body is required";
        if (f.code() == null || f.code().isBlank()) return "Code is required";
        if (f.code().length() > 30) return "Code must be at most 30 characters";
        if (f.name() == null || f.name().isBlank()) return "Name is required";
        if (f.name().length() > 150) return "Name must be at most 150 characters";
        if (f.typeId() == null) return "Org unit type is required";
        return null;   // nature is optional: null = inherit from parent (the organization root stays unclassified)
    }
}
