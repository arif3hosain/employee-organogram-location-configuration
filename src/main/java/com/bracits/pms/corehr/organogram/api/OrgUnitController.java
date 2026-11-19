package com.bracits.pms.corehr.organogram.api;

import com.bracits.pms.corehr.organogram.action.CreateOrgUnitAction;
import com.bracits.pms.corehr.organogram.action.DeleteOrgUnitAction;
import com.bracits.pms.corehr.organogram.action.DeleteOrgUnitPostAction;
import com.bracits.pms.corehr.organogram.action.GetOrgUnitAction;
import com.bracits.pms.corehr.organogram.action.GetOrganogramTreeAction;
import com.bracits.pms.corehr.organogram.action.ListOrgUnitPostsAction;
import com.bracits.pms.corehr.organogram.action.SaveOrgUnitPostAction;
import com.bracits.pms.corehr.organogram.action.SearchOrgUnitsAction;
import com.bracits.pms.corehr.organogram.action.UpdateOrgUnitAction;
import com.bracits.pms.corehr.organogram.domain.OrgUnitNature;
import com.bracits.pms.shared.action.Result;
import com.bracits.pms.shared.action.UpdateCommand;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Organogram (org units and sanctioned posts)")
@RestController
@RequestMapping("/api/org-units")
@RequiredArgsConstructor
public class OrgUnitController {

    private final SearchOrgUnitsAction searchAction;
    private final GetOrganogramTreeAction treeAction;
    private final GetOrgUnitAction getAction;
    private final CreateOrgUnitAction createAction;
    private final UpdateOrgUnitAction updateAction;
    private final DeleteOrgUnitAction deleteAction;
    private final ListOrgUnitPostsAction listPostsAction;
    private final SaveOrgUnitPostAction savePostAction;
    private final DeleteOrgUnitPostAction deletePostAction;

    /** e.g. /api/org-units?nature=NON_FUNCTIONAL or ?typeId=3 (all departments). */
    @GetMapping
    public ResponseEntity<Result<List<OrgUnitView>>> search(@RequestParam(required = false) Long typeId,
                                                            @RequestParam(required = false) Long parentId,
                                                            @RequestParam(required = false) OrgUnitNature nature,
                                                            @RequestParam(required = false) String q) {
        return ApiResponses.of(searchAction.run(new OrgUnitSearch(typeId, parentId, nature, q)));
    }

    @GetMapping("/tree")
    public ResponseEntity<Result<List<OrgUnitNode>>> tree(@RequestParam(required = false) Long rootId) {
        return ApiResponses.of(treeAction.run(rootId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<OrgUnitView>> get(@PathVariable Long id) {
        return ApiResponses.of(getAction.run(id));
    }

    @PostMapping
    public ResponseEntity<Result<OrgUnitView>> create(@RequestBody OrgUnitForm form) {
        return ApiResponses.created(createAction.run(form));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<OrgUnitView>> update(@PathVariable Long id, @RequestBody OrgUnitForm form) {
        return ApiResponses.of(updateAction.run(new UpdateCommand<>(id, form)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Long>> delete(@PathVariable Long id) {
        return ApiResponses.of(deleteAction.run(id));
    }

    /* ---------------- sanctioned posts of a unit ---------------- */

    @GetMapping("/{id}/posts")
    public ResponseEntity<Result<List<OrgUnitPostView>>> posts(@PathVariable Long id) {
        return ApiResponses.of(listPostsAction.run(id));
    }

    /** Body: { "designationId": 5, "sanctionedCount": 3 }. Creates or updates the line. */
    @PutMapping("/{id}/posts")
    public ResponseEntity<Result<OrgUnitPostView>> savePost(@PathVariable Long id, @RequestBody OrgUnitPostForm form) {
        var f = form == null ? null : new OrgUnitPostForm(id, form.designationId(), form.sanctionedCount());
        return ApiResponses.of(savePostAction.run(f));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Result<Long>> deletePost(@PathVariable Long postId) {
        return ApiResponses.of(deletePostAction.run(postId));
    }
}
