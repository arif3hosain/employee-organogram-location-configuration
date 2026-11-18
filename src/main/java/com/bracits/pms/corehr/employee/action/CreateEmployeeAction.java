package com.bracits.pms.corehr.employee.action;

import com.bracits.pms.corehr.employee.api.EmployeeForm;
import com.bracits.pms.corehr.employee.api.EmployeeView;
import com.bracits.pms.corehr.employee.service.EmployeeService;
import com.bracits.pms.shared.action.BaseAction;
import com.bracits.pms.shared.notification.NotificationService;
import org.springframework.stereotype.Component;

/**
 * Reference example of the guide §3.4 flow:
 * preCondition = validate; execute = one transactional service call; postCondition = mail after commit.
 */
@Component
public class CreateEmployeeAction extends BaseAction<EmployeeForm, EmployeeView> {

    private final EmployeeService service;
    private final EmployeeFormRules rules;
    private final NotificationService notificationService;

    public CreateEmployeeAction(EmployeeService service, EmployeeFormRules rules, NotificationService notificationService) {
        this.service = service;
        this.rules = rules;
        this.notificationService = notificationService;
    }

    @Override
    protected String preCondition(EmployeeForm f) {
        String error = rules.check(f);
        if (error != null) return error;
        if (f.supervisorId() != null && !service.exists(f.supervisorId())) return "Supervisor not found: " + f.supervisorId();
        if (service.codeExists(f.employeeCode(), null)) return "Employee code already exists: " + f.employeeCode();
        if (service.emailExists(f.email(), null)) return "Email already used by another employee: " + f.email();
        return null;
    }

    @Override
    protected EmployeeView execute(EmployeeForm f) {
        return service.create(f);                          // the only DB write; commits when it returns
    }

    @Override
    protected void postCondition(EmployeeForm f, EmployeeView e) {
        notificationService.send(e.email(), "Welcome to " + e.orgUnitName(),
                "Dear " + e.fullName() + ", your employee code is " + e.employeeCode() + ".");
        if (e.supervisorId() != null) {
            notificationService.send("supervisor:" + e.supervisorId(), "New team member",
                    e.fullName() + " (" + e.designationName() + ") now reports to you.");
        }
    }

    @Override
    protected String successMessage(EmployeeView e) {
        return "Employee " + e.employeeCode() + " created";
    }
}
