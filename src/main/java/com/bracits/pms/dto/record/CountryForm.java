package com.bracits.pms.dto.record;

import com.bracits.pms.domain.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body for creating or updating a country (the id comes from the URL on update). Codes follow ISO 3166 / ISO 4217. */
public record CountryForm(
        @NotBlank(message = "ISO2 code is required")
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "ISO2 code must be 2 letters (e.g. BD)")
        String iso2,

        @NotBlank(message = "ISO3 code is required")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "ISO3 code must be 3 letters (e.g. BGD)")
        String iso3,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency code must be 3 letters (e.g. BDT)")
        String currencyCode,

        @Pattern(regexp = "^\\+[0-9]{1,4}$", message = "Phone code must look like +880")
        String phoneCode,

        RecordStatus status,
        Long version) {
}
