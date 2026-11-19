package com.bracits.pms.corehr.organogram.action;

import com.bracits.pms.corehr.organogram.api.GradeView;
import com.bracits.pms.corehr.organogram.service.GradeService;
import com.bracits.pms.shared.action.BaseAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListGradesAction extends BaseAction<Void, List<GradeView>> {

    private final GradeService service;

    @Override
    protected List<GradeView> execute(Void input) {
        return service.list();
    }
}
