package com.bracits.pms.corehr.organogram.api;

import com.bracits.pms.corehr.organogram.action.DeleteOrgUnitTypeAction;
import com.bracits.pms.corehr.organogram.action.ListOrgUnitTypesAction;
import com.bracits.pms.corehr.organogram.action.SaveOrgUnitTypeAction;
import com.bracits.pms.shared.action.Result;
import com.bracits.pms.shared.api.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Org unit types")
@RestController
@RequestMapping("/api/org-unit-types")
public class OrgUnitTypeController {

    private final ListOrgUnitTypesAction listAction;
    private final SaveOrgUnitTypeAction saveAction;
    private final DeleteOrgUnitTypeAction deleteAction;

    public OrgUnitTypeController(ListOrgUnitTypesAction listAction, SaveOrgUnitTypeAction saveAction,
                                 DeleteOrgUnitTypeAction deleteAction) {
        this.listAction = listAction;
        this.saveAction = saveAction;
        this.deleteAction = deleteAction;
    }

    @GetMapping
    public ResponseEntity<Result<List<OrgUnitTypeView>>> list() {
        return ApiResponses.of(listAction.run(null));
    }

    @PostMapping
    public ResponseEntity<Result<OrgUnitTypeView>> create(@RequestBody OrgUnitTypeForm form) {
        var f = form == null ? null : new OrgUnitTypeForm(null, form.code(), form.name(), form.levelNo(), form.status(), null);
        return ApiResponses.created(saveAction.run(f));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<OrgUnitTypeView>> update(@PathVariable Long id, @RequestBody OrgUnitTypeForm form) {
        var f = form == null ? null : new OrgUnitTypeForm(id, form.code(), form.name(), form.levelNo(), form.status(), form.version());
        return ApiResponses.of(saveAction.run(f));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Long>> delete(@PathVariable Long id) {
        return ApiResponses.of(deleteAction.run(id));
    }
}
