package com.bracits.pms.corehr.location.api;

import com.bracits.pms.corehr.location.action.DeleteLocationTypeAction;
import com.bracits.pms.corehr.location.action.ListLocationTypesAction;
import com.bracits.pms.corehr.location.action.SaveLocationTypeAction;
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

/** Controller only: receive request, call action.run(), return Result. No business rules here. */
@Tag(name = "Location types")
@RestController
@RequestMapping("/api/location-types")
public class LocationTypeController {

    private final ListLocationTypesAction listAction;
    private final SaveLocationTypeAction saveAction;
    private final DeleteLocationTypeAction deleteAction;

    public LocationTypeController(ListLocationTypesAction listAction, SaveLocationTypeAction saveAction,
                                  DeleteLocationTypeAction deleteAction) {
        this.listAction = listAction;
        this.saveAction = saveAction;
        this.deleteAction = deleteAction;
    }

    @GetMapping
    public ResponseEntity<Result<List<LocationTypeView>>> list() {
        return ApiResponses.of(listAction.run(null));
    }

    @PostMapping
    public ResponseEntity<Result<LocationTypeView>> create(@RequestBody LocationTypeForm form) {
        var f = form == null ? null : new LocationTypeForm(null, form.code(), form.name(), form.levelNo(), form.status(), null);
        return ApiResponses.created(saveAction.run(f));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<LocationTypeView>> update(@PathVariable Long id, @RequestBody LocationTypeForm form) {
        var f = form == null ? null : new LocationTypeForm(id, form.code(), form.name(), form.levelNo(), form.status(), form.version());
        return ApiResponses.of(saveAction.run(f));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Long>> delete(@PathVariable Long id) {
        return ApiResponses.of(deleteAction.run(id));
    }
}
