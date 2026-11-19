package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.api.DesignationView;
import com.bracits.pms.corehr.organogram.service.DesignationService;
import com.bracits.pms.shared.action.BaseAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListDesignationsAction extends BaseAction<Void, List<DesignationView>> {

    private final DesignationService service;

    @Override
    protected List<DesignationView> execute(Void input) {
        return service.list();
    }
}
