package com.bracits.pms.dto.record;

import com.bracits.pms.domain.Country;
import com.bracits.pms.domain.RecordStatus;

public record CountryView(Long id, String iso2, String iso3, String name, String currencyCode, String phoneCode,
                          RecordStatus status, Long version) {

    public static CountryView from(Country c) {
        return new CountryView(c.getId(), c.getIso2(), c.getIso3(), c.getName(), c.getCurrencyCode(), c.getPhoneCode(),
                c.getStatus(), c.getVersion());
    }
}
