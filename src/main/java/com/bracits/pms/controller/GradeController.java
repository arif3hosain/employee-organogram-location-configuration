package com.bracits.pms.controller;

import com.bracits.pms.dto.record.ApiResponse;
import com.bracits.pms.dto.ApiResponses;
import com.bracits.pms.dto.record.GradeForm;
import com.bracits.pms.dto.record.GradeView;
import com.bracits.pms.service.GradeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

/** Thin controller: Bean Validation on the body, then one transactional service call. */
@Tag(name = "Grades")
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GradeView>>> list() {
        return ApiResponses.ok(service.list());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GradeView>> create(@Valid @RequestBody GradeForm form) {
        return ApiResponses.created("Grade saved", service.create(form));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GradeView>> update(@PathVariable Long id, @Valid @RequestBody GradeForm form) {
        return ApiResponses.ok("Grade saved", service.update(id, form));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Long>> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponses.ok("Grade deleted", id);
    }
}
