package com.bracits.pms.corehr.employee.event;

import com.bracits.pms.corehr.employee.api.EmployeeView;

/** Published by EmployeeService.delete with a snapshot of the removed employee; delivered after commit. */
public record EmployeeDeletedEvent(EmployeeView employee) {
}
