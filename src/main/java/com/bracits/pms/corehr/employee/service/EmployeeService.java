package com.bracits.pms.corehr.employee.service;

import com.bracits.pms.corehr.employee.api.EmployeeForm;
import com.bracits.pms.corehr.employee.api.EmployeeSearch;
import com.bracits.pms.corehr.employee.api.EmployeeView;
import com.bracits.pms.corehr.employee.domain.Employee;
import com.bracits.pms.corehr.employee.infrastructure.EmployeeRepository;
import com.bracits.pms.corehr.location.domain.Location;
import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.corehr.organogram.domain.Designation;
import com.bracits.pms.corehr.organogram.domain.OrgUnit;
import com.bracits.pms.corehr.organogram.service.DesignationService;
import com.bracits.pms.corehr.organogram.service.OrgUnitService;
import com.bracits.pms.shared.api.PageView;
import com.bracits.pms.shared.domain.RecordStatus;
import com.bracits.pms.shared.error.BusinessException;
import com.bracits.pms.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The only transactional layer for employees. Other sub-modules (location, organogram)
 * are reached through their services, never their repositories.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private static final int MAX_SUPERVISOR_DEPTH = 50;

    private final EmployeeRepository repo;
    private final DesignationService designationService;
    private final OrgUnitService orgUnitService;
    private final LocationService locationService;

    /* ------------------------------ queries ------------------------------ */

    @Transactional(readOnly = true)
    public EmployeeView get(Long id) {
        return EmployeeView.from(load(id));
    }

    @Transactional(readOnly = true)
    public PageView<EmployeeView> search(EmployeeSearch s) {
        // LIKE patterns are never null: "%" means "no filter" (see EmployeeRepository.search)
        String qPattern = s.q() == null || s.q().isBlank() ? "%" : "%" + s.q().trim().toLowerCase() + "%";
        String orgPattern = s.orgUnitId() == null ? "%" : orgUnitService.get(s.orgUnitId()).path() + "%";
        String locationPattern = s.locationId() == null ? "%" : locationService.get(s.locationId()).path() + "%";
        var pageable = PageRequest.of(s.page(), s.size(), Sort.by("employeeCode"));
        return PageView.of(repo.search(qPattern, orgPattern, locationPattern, s.designationId(), s.status(), pageable),
                EmployeeView::from);
    }

    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return repo.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean codeExists(String code, Long excludeId) {
        return excludeId == null
                ? repo.existsByEmployeeCodeIgnoreCase(code.trim())
                : repo.existsByEmployeeCodeIgnoreCaseAndIdNot(code.trim(), excludeId);
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email, Long excludeId) {
        return excludeId == null
                ? repo.existsByEmailIgnoreCase(email.trim())
                : repo.existsByEmailIgnoreCaseAndIdNot(email.trim(), excludeId);
    }

    @Transactional(readOnly = true)
    public boolean hasSubordinates(Long id) {
        return repo.existsBySupervisorId(id);
    }

    /* ------------------------------ commands ----------------------------- */

    @Transactional
    public EmployeeView create(EmployeeForm f) {
        checkUnique(f, null);
        Employee e = new Employee(f.employeeCode(), f.fullName(), f.email(), f.phone(), f.joinDate(),
                designation(f), orgUnit(f), location(f), supervisor(f.supervisorId()));
        return EmployeeView.from(repo.save(e));
    }

    @Transactional
    public EmployeeView update(Long id, EmployeeForm f) {
        Employee e = load(id);
        e.assertVersion(f.version());
        checkUnique(f, id);
        Employee supervisor = supervisor(f.supervisorId());
        checkNoReportingCycle(e, supervisor);
        if (f.status() == RecordStatus.INACTIVE && e.getStatus() == RecordStatus.ACTIVE && repo.existsBySupervisorId(id)) {
            throw new BusinessException("Reassign this employee's subordinates before making them INACTIVE");
        }
        e.change(f.employeeCode(), f.fullName(), f.email(), f.phone(), f.joinDate(),
                sameOr(e.getDesignation(), f.designationId(), () -> designation(f)),
                sameOr(e.getOrgUnit(), f.orgUnitId(), () -> orgUnit(f)),
                sameOr(e.getLocation(), f.locationId(), () -> location(f)),
                supervisor, f.status());
        return EmployeeView.from(e);
    }

    /** Returns the deleted employee's view so postCondition can still use name and email. */
    @Transactional
    public EmployeeView delete(Long id) {
        Employee e = load(id);
        if (repo.existsBySupervisorId(id)) {
            throw new BusinessException("Employee supervises other employees. Reassign them first.");
        }
        EmployeeView view = EmployeeView.from(e);
        repo.delete(e);
        repo.flush();
        return view;
    }

    /* ------------------------------ helpers ------------------------------ */

    private Employee load(Long id) {
        return repo.findFull(id).orElseThrow(() -> new NotFoundException("Employee", id));
    }

    private void checkUnique(EmployeeForm f, Long excludeId) {
        if (codeExists(f.employeeCode(), excludeId)) {
            throw new BusinessException("Employee code already exists: " + f.employeeCode());
        }
        if (emailExists(f.email(), excludeId)) {
            throw new BusinessException("Email already used by another employee: " + f.email());
        }
    }

    private Designation designation(EmployeeForm f) {
        return designationService.findActiveEntity(f.designationId());
    }

    private OrgUnit orgUnit(EmployeeForm f) {
        return orgUnitService.findActiveEntity(f.orgUnitId());
    }

    private Location location(EmployeeForm f) {
        return locationService.findActiveEntity(f.locationId());
    }

    private Employee supervisor(Long supervisorId) {
        if (supervisorId == null) {
            return null;
        }
        Employee s = repo.findById(supervisorId).orElseThrow(() -> new NotFoundException("Supervisor", supervisorId));
        if (s.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException("Supervisor is inactive: " + s.getEmployeeCode());
        }
        return s;
    }

    /** Walk up from the new supervisor; reaching the employee again means a loop (A -> B -> A). */
    private static void checkNoReportingCycle(Employee employee, Employee newSupervisor) {
        Employee cursor = newSupervisor;
        for (int depth = 0; cursor != null; depth++) {
            if (cursor.getId().equals(employee.getId())) {
                throw new BusinessException("Reporting line would form a loop: an employee cannot report to themselves or to their own subordinate");
            }
            if (depth > MAX_SUPERVISOR_DEPTH) {
                throw new BusinessException("Reporting line is deeper than " + MAX_SUPERVISOR_DEPTH + " levels");
            }
            cursor = cursor.getSupervisor();
        }
    }

    /** Keep the current reference when the id did not change (avoids re-validating an inactive-but-kept link). */
    private static <T extends com.bracits.pms.shared.domain.BaseEntity> T sameOr(T current, Long newId,
                                                                               java.util.function.Supplier<T> loader) {
        return current != null && current.getId().equals(newId) ? current : loader.get();
    }
}
