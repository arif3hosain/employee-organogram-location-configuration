package com.bracits.pms.corehr.employee.domain;

import com.bracits.pms.corehr.location.domain.Location;
import com.bracits.pms.corehr.organogram.domain.Designation;
import com.bracits.pms.corehr.organogram.domain.OrgUnit;
import com.bracits.pms.shared.domain.BaseEntity;
import com.bracits.pms.shared.domain.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Employee placed in the organogram (orgUnit + designation), at a workplace (location),
 * reporting to a supervisor (the PMS reporting line).
 */
@Entity
@Table(name = "employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee extends BaseEntity {

    @Column(name = "employee_code", nullable = false, unique = true, length = 30)
    private String employeeCode;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "designation_id", nullable = false)
    private Designation designation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_unit_id", nullable = false)
    private OrgUnit orgUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private Employee supervisor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    public Employee(String employeeCode, String fullName, String email, String phone, LocalDate joinDate,
                    Designation designation, OrgUnit orgUnit, Location location, Employee supervisor) {
        change(employeeCode, fullName, email, phone, joinDate, designation, orgUnit, location, supervisor, RecordStatus.ACTIVE);
    }

    public void change(String employeeCode, String fullName, String email, String phone, LocalDate joinDate,
                       Designation designation, OrgUnit orgUnit, Location location, Employee supervisor,
                       RecordStatus status) {
        this.employeeCode = employeeCode.trim().toUpperCase();
        this.fullName = fullName.trim();
        this.email = email.trim().toLowerCase();
        this.phone = phone == null || phone.isBlank() ? null : phone.trim();
        this.joinDate = joinDate;
        this.designation = designation;
        this.orgUnit = orgUnit;
        this.location = location;
        this.supervisor = supervisor;
        this.status = status == null ? RecordStatus.ACTIVE : status;
    }
}
