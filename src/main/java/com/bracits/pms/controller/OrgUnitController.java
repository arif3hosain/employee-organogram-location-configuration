package com.bracits.pms.controller;

import com.bracits.pms.domain.OrgUnitNature;
import com.bracits.pms.dto.record.ApiResponse;
import com.bracits.pms.dto.ApiResponses;
import com.bracits.pms.dto.record.OrgUnitForm;
import com.bracits.pms.dto.record.OrgUnitNode;
import com.bracits.pms.dto.record.OrgUnitPostForm;
import com.bracits.pms.dto.record.OrgUnitPostView;
import com.bracits.pms.dto.record.OrgUnitSearch;
import com.bracits.pms.dto.record.OrgUnitView;
import com.bracits.pms.service.OrgUnitPostService;
import com.bracits.pms.service.OrgUnitService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Thin controller: Bean Validation on the body, then one transactional service call. */
@Tag(name = "Organogram (org units and sanctioned posts)")
@RestController
@RequestMapping("/api/org-units")
@RequiredArgsConstructor
public class OrgUnitController {

    private final OrgUnitService service;
    private final OrgUnitPostService postService;

    /** e.g. /api/org-units?nature=NON_FUNCTIONAL or ?typeId=3 (all departments). */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrgUnitView>>> search(@RequestParam(required = false) Long typeId,
                                                                 @RequestParam(required = false) Long parentId,
                                                                 @RequestParam(required = false) OrgUnitNature nature,
                                                                 @RequestParam(required = false) String q) {
        return ApiResponses.ok(service.search(new OrgUnitSearch(typeId, parentId, nature, q)));
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<OrgUnitNode>>> tree(@RequestParam(required = false) Long rootId) {
        return ApiResponses.ok(service.tree(rootId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrgUnitView>> get(@PathVariable Long id) {
        return ApiResponses.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrgUnitView>> create(@Valid @RequestBody OrgUnitForm form) {
        OrgUnitView v = service.create(form);
        return ApiResponses.created("Org unit " + v.code() + " created", v);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrgUnitView>> update(@PathVariable Long id, @Valid @RequestBody OrgUnitForm form) {
        OrgUnitView v = service.update(id, form);
        return ApiResponses.ok("Org unit " + v.code() + " updated", v);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Long>> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponses.ok("Org unit deleted", id);
    }

    /* ---------------- sanctioned posts of a unit ---------------- */

    @GetMapping("/{id}/posts")
    public ResponseEntity<ApiResponse<List<OrgUnitPostView>>> posts(@PathVariable Long id) {
        return ApiResponses.ok(postService.listByOrgUnit(id));
    }

    /** Body: { "designationId": 5, "sanctionedCount": 3 }. Creates or updates the line. */
    @PutMapping("/{id}/posts")
    public ResponseEntity<ApiResponse<OrgUnitPostView>> savePost(@PathVariable Long id, @Valid @RequestBody OrgUnitPostForm form) {
        OrgUnitPostView v = postService.save(id, form);
        String message = v.vacancy() < 0
                ? "Saved. Warning: " + (-v.vacancy()) + " more active employee(s) than sanctioned posts"
                : "Sanctioned posts saved";
        return ApiResponses.ok(message, v);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Long>> deletePost(@PathVariable Long postId) {
        postService.delete(postId);
        return ApiResponses.ok("Sanctioned post line deleted", postId);
    }
}
