package com.bracits.pms.corehr.location.action;

import com.bracits.pms.corehr.location.api.LocationForm;

/** Field checks shared by CreateLocationAction and UpdateLocationAction. */
final class LocationFormRules {

    private LocationFormRules() {
    }

    static String check(LocationForm f) {
        if (f == null) return "Request body is required";
        if (f.code() == null || f.code().isBlank()) return "Code is required";
        if (f.code().length() > 30) return "Code must be at most 30 characters";
        if (f.name() == null || f.name().isBlank()) return "Name is required";
        if (f.name().length() > 150) return "Name must be at most 150 characters";
        if (f.typeId() == null) return "Location type is required";
        return null;
    }
}
