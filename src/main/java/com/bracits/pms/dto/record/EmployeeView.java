package com.bracits.pms.dto.record;

import com.bracits.pms.domain.Employee;
import com.bracits.pms.domain.RecordStatus;
import java.time.LocalDate;

public record EmployeeView(Long id, String employeeCode, String fullName, String email, String phone,
                           LocalDate joinDate,       
                           Long designationId, String designationName,
                           Long gradeId, String gradeCode, String gradeName,
                           Long orgUnitId, String orgUnitName,
                           Long locationId, String locationName,
                           Long supervisorId, String supervisorName,
                           RecordStatus status, Long version) {

    /** Must be called inside a transaction (associations are lazy). */
    public static EmployeeView from(Employee e) {
        var s = e.getSupervisor();
        return new EmployeeView(e.getId(), e.getEmployeeCode(), e.getFullName(), e.getEmail(), e.getPhone(),
                e.getJoinDate(),
                e.getDesignation().getId(), e.getDesignation().getName(),
                e.getDesignation().getGrade().getId(), e.getDesignation().getGrade().getCode(),
                e.getDesignation().getGrade().getName(),
                e.getOrgUnit().getId(), e.getOrgUnit().getName(),
                e.getLocation().getId(), e.getLocation().getName(),
                s == null ? null : s.getId(), s == null ? null : s.getFullName(),
                e.getStatus(), e.getVersion());
    }
}
