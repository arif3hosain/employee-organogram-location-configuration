package com.bracits.pms.corehr.location.api;

import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.shared.api.ApiResponse;
import com.bracits.pms.shared.api.ApiResponses;
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
@Tag(name = "Locations")
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService service;

    /** e.g. /api/locations?typeId=6 (all districts) or ?parentId=12 (the districts of Dhaka division). */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LocationView>>> search(@RequestParam(required = false) Long typeId,
                                                                  @RequestParam(required = false) Long parentId,
                                                                  @RequestParam(required = false) String q) {
        return ApiResponses.ok(service.search(new LocationSearch(typeId, parentId, q)));
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<LocationNode>>> tree(@RequestParam(required = false) Long rootId) {
        return ApiResponses.ok(service.tree(rootId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationView>> get(@PathVariable Long id) {
        return ApiResponses.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LocationView>> create(@Valid @RequestBody LocationForm form) {
        LocationView v = service.create(form);
        return ApiResponses.created("Location " + v.code() + " created", v);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationView>> update(@PathVariable Long id, @Valid @RequestBody LocationForm form) {
        LocationView v = service.update(id, form);
        return ApiResponses.ok("Location " + v.code() + " updated", v);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Long>> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponses.ok("Location deleted", id);
    }
}
