package com.bracits.pms.corehr.employee.action;

import com.bracits.pms.corehr.employee.api.EmployeeForm;
import com.bracits.pms.corehr.employee.api.EmployeeView;
import com.bracits.pms.corehr.employee.service.EmployeeService;
import com.bracits.pms.shared.action.BaseAction;
import com.bracits.pms.shared.action.UpdateCommand;
import org.springframework.stereotype.Component;

@Component
public class UpdateEmployeeAction extends BaseAction<UpdateCommand<EmployeeForm>, EmployeeView> {

    private final EmployeeService service;
    private final EmployeeFormRules rules;

    public UpdateEmployeeAction(EmployeeService service, EmployeeFormRules rules) {
        this.service = service;
        this.rules = rules;
    }

    @Override
    protected String preCondition(UpdateCommand<EmployeeForm> c) {
        String error = rules.check(c.form());
        if (error != null) return error;
        EmployeeForm f = c.form();
        if (!service.exists(c.id())) return "Employee not found: " + c.id();
        if (f.version() == null) return "Version is required for update (send the version you loaded)";
        if (c.id().equals(f.supervisorId())) return "An employee cannot be their own supervisor";
        if (f.supervisorId() != null && !service.exists(f.supervisorId())) return "Supervisor not found: " + f.supervisorId();
        if (service.codeExists(f.employeeCode(), c.id())) return "Employee code already exists: " + f.employeeCode();
        if (service.emailExists(f.email(), c.id())) return "Email already used by another employee: " + f.email();
        return null;   // reporting-loop check needs the whole chain, so it runs in the service
    }

    @Override
    protected EmployeeView execute(UpdateCommand<EmployeeForm> c) {
        return service.update(c.id(), c.form());
    }

    @Override
    protected String successMessage(EmployeeView e) {
        return "Employee " + e.employeeCode() + " updated";
    }
}
