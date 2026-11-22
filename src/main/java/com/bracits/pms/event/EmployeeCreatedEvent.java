package com.bracits.pms.event;

import com.bracits.pms.dto.record.EmployeeView;

/** Published by EmployeeService.create inside its transaction; delivered to listeners after commit. */
public record EmployeeCreatedEvent(EmployeeView employee) {
}
