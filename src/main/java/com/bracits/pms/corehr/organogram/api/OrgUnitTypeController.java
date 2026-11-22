package com.bracits.pms.corehr.organogram.api;

import com.bracits.pms.corehr.organogram.service.OrgUnitTypeService;
import com.bracits.pms.shared.api.ApiResponse;
import com.bracits.pms.shared.api.ApiResponses;
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
@Tag(name = "Org unit types")
@RestController
@RequestMapping("/api/org-unit-types")
@RequiredArgsConstructor
public class OrgUnitTypeController {

    private final OrgUnitTypeService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrgUnitTypeView>>> list() {
        return ApiResponses.ok(service.list());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrgUnitTypeView>> create(@Valid @RequestBody OrgUnitTypeForm form) {
        return ApiResponses.created("Org unit type saved", service.create(form));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrgUnitTypeView>> update(@PathVariable Long id, @Valid @RequestBody OrgUnitTypeForm form) {
        return ApiResponses.ok("Org unit type saved", service.update(id, form));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Long>> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponses.ok("Org unit type deleted", id);
    }
}
