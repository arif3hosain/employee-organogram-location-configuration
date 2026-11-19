package com.bracits.pms.corehr.employee.action;

import com.bracits.pms.corehr.employee.api.EmployeeSearch;
import com.bracits.pms.corehr.employee.api.EmployeeView;
import com.bracits.pms.corehr.employee.service.EmployeeService;
import com.bracits.pms.shared.action.BaseAction;
import com.bracits.pms.shared.api.PageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchEmployeesAction extends BaseAction<EmployeeSearch, PageView<EmployeeView>> {

    private static final int MAX_PAGE_SIZE = 200;

    private final EmployeeService service;

    @Override
    protected String preCondition(EmployeeSearch s) {
        if (s.page() < 0) return "Page must be 0 or more";
        if (s.size() < 1 || s.size() > MAX_PAGE_SIZE) return "Size must be between 1 and " + MAX_PAGE_SIZE;
        return null;
    }

    @Override
    protected PageView<EmployeeView> execute(EmployeeSearch s) {
        return service.search(s);
    }
}
