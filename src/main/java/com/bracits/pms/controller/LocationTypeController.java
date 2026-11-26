package com.bracits.pms.controller;

import com.bracits.pms.dto.record.ApiResponse;
import com.bracits.pms.dto.ApiResponses;
import com.bracits.pms.dto.record.LocationTypeForm;
import com.bracits.pms.dto.record.LocationTypeView;
import com.bracits.pms.service.LocationTypeService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Thin controller: Bean Validation on the body, then one transactional service call. */
@Tag(name = "Location types")
@RestController
@RequestMapping("/api/location-types")
@RequiredArgsConstructor
public class LocationTypeController {

    private final LocationTypeService service;

    /** All types, or ?countryId=1 for one country's levels. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LocationTypeView>>> list(@RequestParam(required = false) Long countryId) {
        return ApiResponses.ok(service.list(countryId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LocationTypeView>> create(@Valid @RequestBody LocationTypeForm form) {
        return ApiResponses.created("Location type saved", service.create(form));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationTypeView>> update(@PathVariable Long id, @Valid @RequestBody LocationTypeForm form) {
        return ApiResponses.ok("Location type saved", service.update(id, form));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Long>> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponses.ok("Location type deleted", id);
    }
}
