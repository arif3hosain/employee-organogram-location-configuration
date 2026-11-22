package com.bracits.pms.event;

import com.bracits.pms.dto.record.EmployeeView;

/** Published by EmployeeService.delete with a snapshot of the removed employee; delivered after commit. */
public record EmployeeDeletedEvent(EmployeeView employee) {
}
