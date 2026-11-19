package com.bracits.pms.corehr.employee.api;

import com.bracits.pms.corehr.employee.action.CreateEmployeeAction;
import com.bracits.pms.corehr.employee.action.DeleteEmployeeAction;
import com.bracits.pms.corehr.employee.action.GetEmployeeAction;
import com.bracits.pms.corehr.employee.action.SearchEmployeesAction;
import com.bracits.pms.corehr.employee.action.UpdateEmployeeAction;
import com.bracits.pms.shared.action.Result;
import com.bracits.pms.shared.action.UpdateCommand;
import com.bracits.pms.shared.api.ApiResponses;
import com.bracits.pms.shared.api.PageView;
import com.bracits.pms.shared.domain.RecordStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * Controller -> Action only. No validation, no service or repository calls here
 * (enforced by ArchitectureTest).
 */
@Tag(name = "Employees")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final SearchEmployeesAction searchAction;
    private final GetEmployeeAction getAction;
    private final CreateEmployeeAction createAction;
    private final UpdateEmployeeAction updateAction;
    private final DeleteEmployeeAction deleteAction;

    /** e.g. /api/employees?orgUnitId=2 (everyone under the Technology function) or ?locationId=2&status=ACTIVE */
    @GetMapping
    public ResponseEntity<Result<PageView<EmployeeView>>> search(@RequestParam(required = false) String q,
                                                                 @RequestParam(required = false) Long orgUnitId,
                                                                 @RequestParam(required = false) Long locationId,
                                                                 @RequestParam(required = false) Long designationId,
                                                                 @RequestParam(required = false) RecordStatus status,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.of(searchAction.run(new EmployeeSearch(q, orgUnitId, locationId, designationId, status, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<EmployeeView>> get(@PathVariable Long id) {
        return ApiResponses.of(getAction.run(id));
    }

    @PostMapping
    public ResponseEntity<Result<EmployeeView>> create(@RequestBody EmployeeForm form) {
        return ApiResponses.created(createAction.run(form));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<EmployeeView>> update(@PathVariable Long id, @RequestBody EmployeeForm form) {
        return ApiResponses.of(updateAction.run(new UpdateCommand<>(id, form)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<EmployeeView>> delete(@PathVariable Long id) {
        return ApiResponses.of(deleteAction.run(id));
    }
}
