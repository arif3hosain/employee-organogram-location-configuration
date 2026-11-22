package com.bracits.pms.corehr.employee.api;

import com.bracits.pms.corehr.employee.service.EmployeeService;
import com.bracits.pms.shared.api.ApiResponse;
import com.bracits.pms.shared.api.ApiResponses;
import com.bracits.pms.shared.api.OnUpdate;
import com.bracits.pms.shared.api.PageView;
import com.bracits.pms.shared.domain.RecordStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Thin controller: Bean Validation on the body, then one transactional service call. */
@Tag(name = "Employees")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    /** e.g. /api/employees?orgUnitId=2 (everyone under the Technology function) or ?locationId=2&status=ACTIVE */
    @GetMapping
    public ResponseEntity<ApiResponse<PageView<EmployeeView>>> search(@RequestParam(required = false) String q,
                                                                      @RequestParam(required = false) Long orgUnitId,
                                                                      @RequestParam(required = false) Long locationId,
                                                                      @RequestParam(required = false) Long designationId,
                                                                      @RequestParam(required = false) RecordStatus status,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(service.search(new EmployeeSearch(q, orgUnitId, locationId, designationId, status, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeView>> get(@PathVariable Long id) {
        return ApiResponses.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeView>> create(@Valid @RequestBody EmployeeForm form) {
        EmployeeView e = service.create(form);
        return ApiResponses.created("Employee " + e.employeeCode() + " created", e);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeView>> update(@PathVariable Long id,
                                                            @Validated({Default.class, OnUpdate.class}) @RequestBody EmployeeForm form) {
        EmployeeView e = service.update(id, form);
        return ApiResponses.ok("Employee " + e.employeeCode() + " updated", e);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeView>> delete(@PathVariable Long id) {
        EmployeeView e = service.delete(id);
        return ApiResponses.ok("Employee " + e.employeeCode() + " deleted", e);
    }
}
