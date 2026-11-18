package com.bracits.pms.corehr.organogram.api;

import com.bracits.pms.corehr.organogram.action.DeleteDesignationAction;
import com.bracits.pms.corehr.organogram.action.ListDesignationsAction;
import com.bracits.pms.corehr.organogram.action.SaveDesignationAction;
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

@Tag(name = "Designations")
@RestController
@RequestMapping("/api/designations")
public class DesignationController {

    private final ListDesignationsAction listAction;
    private final SaveDesignationAction saveAction;
    private final DeleteDesignationAction deleteAction;

    public DesignationController(ListDesignationsAction listAction, SaveDesignationAction saveAction,
                                 DeleteDesignationAction deleteAction) {
        this.listAction = listAction;
        this.saveAction = saveAction;
        this.deleteAction = deleteAction;
    }

    @GetMapping
    public ResponseEntity<Result<List<DesignationView>>> list() {
        return ApiResponses.of(listAction.run(null));
    }

    @PostMapping
    public ResponseEntity<Result<DesignationView>> create(@RequestBody DesignationForm form) {
        var f = form == null ? null : new DesignationForm(null, form.code(), form.name(), form.gradeId(), form.status(), null);
        return ApiResponses.created(saveAction.run(f));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<DesignationView>> update(@PathVariable Long id, @RequestBody DesignationForm form) {
        var f = form == null ? null : new DesignationForm(id, form.code(), form.name(), form.gradeId(), form.status(), form.version());
        return ApiResponses.of(saveAction.run(f));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Long>> delete(@PathVariable Long id) {
        return ApiResponses.of(deleteAction.run(id));
    }
}
