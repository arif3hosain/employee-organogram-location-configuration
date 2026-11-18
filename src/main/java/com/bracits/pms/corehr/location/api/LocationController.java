package com.bracits.pms.corehr.location.api;

import com.bracits.pms.corehr.location.action.CreateLocationAction;
import com.bracits.pms.corehr.location.action.DeleteLocationAction;
import com.bracits.pms.corehr.location.action.GetLocationAction;
import com.bracits.pms.corehr.location.action.GetLocationTreeAction;
import com.bracits.pms.corehr.location.action.SearchLocationsAction;
import com.bracits.pms.corehr.location.action.UpdateLocationAction;
import com.bracits.pms.shared.action.Result;
import com.bracits.pms.shared.action.UpdateCommand;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Locations")
@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final SearchLocationsAction searchAction;
    private final GetLocationTreeAction treeAction;
    private final GetLocationAction getAction;
    private final CreateLocationAction createAction;
    private final UpdateLocationAction updateAction;
    private final DeleteLocationAction deleteAction;

    public LocationController(SearchLocationsAction searchAction, GetLocationTreeAction treeAction,
                              GetLocationAction getAction, CreateLocationAction createAction,
                              UpdateLocationAction updateAction, DeleteLocationAction deleteAction) {
        this.searchAction = searchAction;
        this.treeAction = treeAction;
        this.getAction = getAction;
        this.createAction = createAction;
        this.updateAction = updateAction;
        this.deleteAction = deleteAction;
    }

    /** e.g. /api/locations?typeId=6 (all districts) or ?parentId=12 (the districts of Dhaka division). */
    @GetMapping
    public ResponseEntity<Result<List<LocationView>>> search(@RequestParam(required = false) Long typeId,
                                                             @RequestParam(required = false) Long parentId,
                                                             @RequestParam(required = false) String q) {
        return ApiResponses.of(searchAction.run(new LocationSearch(typeId, parentId, q)));
    }

    @GetMapping("/tree")
    public ResponseEntity<Result<List<LocationNode>>> tree(@RequestParam(required = false) Long rootId) {
        return ApiResponses.of(treeAction.run(rootId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<LocationView>> get(@PathVariable Long id) {
        return ApiResponses.of(getAction.run(id));
    }

    @PostMapping
    public ResponseEntity<Result<LocationView>> create(@RequestBody LocationForm form) {
        return ApiResponses.created(createAction.run(form));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<LocationView>> update(@PathVariable Long id, @RequestBody LocationForm form) {
        return ApiResponses.of(updateAction.run(new UpdateCommand<>(id, form)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Long>> delete(@PathVariable Long id) {
        return ApiResponses.of(deleteAction.run(id));
    }
}
