package com.bracits.pms.corehr.employee.event;

import com.bracits.pms.corehr.employee.api.EmployeeView;

/** Published by EmployeeService.create inside its transaction; delivered to listeners after commit. */
public record EmployeeCreatedEvent(EmployeeView employee) {
}
