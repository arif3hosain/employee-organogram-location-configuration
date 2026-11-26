package com.bracits.pms.controller;

import com.bracits.pms.dto.ApiResponses;
import com.bracits.pms.dto.record.ApiResponse;
import com.bracits.pms.dto.record.CountryForm;
import com.bracits.pms.dto.record.CountryView;
import com.bracits.pms.service.CountryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Thin controller: Bean Validation on the body, then one transactional service call. */
@Tag(name = "Countries")
@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CountryView>>> list() {
        return ApiResponses.ok(service.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CountryView>> get(@PathVariable Long id) {
        return ApiResponses.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CountryView>> create(@Valid @RequestBody CountryForm form) {
        CountryView v = service.create(form);
        return ApiResponses.created("Country " + v.name() + " created", v);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CountryView>> update(@PathVariable Long id, @Valid @RequestBody CountryForm form) {
        CountryView v = service.update(id, form);
        return ApiResponses.ok("Country " + v.name() + " updated", v);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Long>> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponses.ok("Country deleted", id);
    }
}
