package com.bracits.pms.controller;

import com.bracits.pms.dto.record.ApiResponse;
import com.bracits.pms.dto.ApiResponses;
import com.bracits.pms.dto.record.DesignationForm;
import com.bracits.pms.dto.record.DesignationView;
import com.bracits.pms.service.DesignationService;
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
@Tag(name = "Designations")
@RestController
@RequestMapping("/api/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DesignationView>>> list() {
        return ApiResponses.ok(service.list());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DesignationView>> create(@Valid @RequestBody DesignationForm form) {
        return ApiResponses.created("Designation saved", service.create(form));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DesignationView>> update(@PathVariable Long id, @Valid @RequestBody DesignationForm form) {
        return ApiResponses.ok("Designation saved", service.update(id, form));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Long>> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponses.ok("Designation deleted", id);
    }
}
