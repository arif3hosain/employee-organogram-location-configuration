package com.bracits.pms.corehr.employee.action;

import com.bracits.pms.corehr.employee.api.EmployeeView;
import com.bracits.pms.corehr.employee.service.EmployeeService;
import com.bracits.pms.shared.action.BaseAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetEmployeeAction extends BaseAction<Long, EmployeeView> {

    private final EmployeeService service;

    @Override
    protected EmployeeView execute(Long id) {
        return service.get(id);
    }
}
