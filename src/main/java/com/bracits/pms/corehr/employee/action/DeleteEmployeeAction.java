package com.bracits.pms.corehr.employee.action;

import com.bracits.pms.corehr.employee.api.EmployeeView;
import com.bracits.pms.corehr.employee.service.EmployeeService;
import com.bracits.pms.shared.action.BaseAction;
import com.bracits.pms.shared.notification.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class DeleteEmployeeAction extends BaseAction<Long, EmployeeView> {

    private final EmployeeService service;
    private final NotificationService notificationService;

    public DeleteEmployeeAction(EmployeeService service, NotificationService notificationService) {
        this.service = service;
        this.notificationService = notificationService;
    }

    @Override
    protected String preCondition(Long id) {
        if (id == null) return "Id is required";
        if (!service.exists(id)) return "Employee not found: " + id;
        if (service.hasSubordinates(id)) return "Employee supervises other employees. Reassign them first.";
        return null;
    }

    @Override
    protected EmployeeView execute(Long id) {
        return service.delete(id);
    }

    @Override
    protected void postCondition(Long id, EmployeeView deleted) {
        notificationService.send("hr-admin", "Employee removed",
                deleted.employeeCode() + " " + deleted.fullName() + " was deleted.");
    }

    @Override
    protected String successMessage(EmployeeView deleted) {
        return "Employee " + deleted.employeeCode() + " deleted";
    }
}
