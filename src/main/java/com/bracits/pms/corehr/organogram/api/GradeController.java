package com.bracits.pms.corehr.organogram.api;

import com.bracits.pms.corehr.organogram.action.DeleteGradeAction;
import com.bracits.pms.corehr.organogram.action.ListGradesAction;
import com.bracits.pms.corehr.organogram.action.SaveGradeAction;
import com.bracits.pms.shared.action.Result;
import com.bracits.pms.shared.api.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

@Tag(name = "Grades")
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final ListGradesAction listAction;
    private final SaveGradeAction saveAction;
    private final DeleteGradeAction deleteAction;

    @GetMapping
    public ResponseEntity<Result<List<GradeView>>> list() {
        return ApiResponses.of(listAction.run(null));
    }

    @PostMapping
    public ResponseEntity<Result<GradeView>> create(@RequestBody GradeForm form) {
        var f = form == null ? null : new GradeForm(null, form.code(), form.name(), form.rankNo(), form.status(), null);
        return ApiResponses.created(saveAction.run(f));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<GradeView>> update(@PathVariable Long id, @RequestBody GradeForm form) {
        var f = form == null ? null : new GradeForm(id, form.code(), form.name(), form.rankNo(), form.status(), form.version());
        return ApiResponses.of(saveAction.run(f));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Long>> delete(@PathVariable Long id) {
        return ApiResponses.of(deleteAction.run(id));
    }
}
