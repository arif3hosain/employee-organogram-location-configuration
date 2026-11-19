package com.bracits.pms.corehr.location.service;

import com.bracits.pms.corehr.location.api.LocationForm;
import com.bracits.pms.corehr.location.api.LocationNode;
import com.bracits.pms.corehr.location.api.LocationSearch;
import com.bracits.pms.corehr.location.api.LocationView;
import com.bracits.pms.corehr.location.domain.Location;
import com.bracits.pms.corehr.location.domain.LocationType;
import com.bracits.pms.corehr.location.infrastructure.LocationRepository;
import com.bracits.pms.shared.domain.HierarchyPath;
import com.bracits.pms.shared.domain.RecordStatus;
import com.bracits.pms.shared.error.BusinessException;
import com.bracits.pms.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository repo;
    private final LocationTypeService typeService;

    /* ------------------------------ queries ------------------------------ */

    @Transactional(readOnly = true)
    public LocationView get(Long id) {
        return LocationView.from(load(id));
    }

    @Transactional(readOnly = true)
    public List<LocationView> search(LocationSearch s) {
        String q = s.q() == null || s.q().isBlank() ? "%" : "%" + s.q().trim().toLowerCase() + "%";   // never null (PostgreSQL)
        return repo.search(s.typeId(), s.parentId(), q).stream().map(LocationView::from).toList();
    }

    /** Whole tree, or the subtree under rootId. */
    @Transactional(readOnly = true)
    public List<LocationNode> tree(Long rootId) {
        List<Location> rows = rootId == null ? repo.findAllWithType() : repo.findSubtree(load(rootId).getPath());
        Map<Long, LocationNode> byId = new LinkedHashMap<>();
        List<LocationNode> roots = new ArrayList<>();
        for (Location l : rows) {                          // ordered by path, so parents come first
            LocationNode node = LocationNode.of(LocationView.from(l));
            byId.put(l.getId(), node);
            LocationNode parent = l.getParent() == null ? null : byId.get(l.getParent().getId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.children().add(node);
            }
        }
        return roots;
    }

    @Transactional(readOnly = true)
    public boolean codeExists(String code, Long excludeId) {
        return excludeId == null
                ? repo.existsByCodeIgnoreCase(code.trim())
                : repo.existsByCodeIgnoreCaseAndIdNot(code.trim(), excludeId);
    }

    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return repo.existsById(id);
    }

    /* ------------------------------ commands ----------------------------- */

    @Transactional
    public LocationView create(LocationForm f) {
        LocationType type = activeType(f.typeId());
        Location parent = f.parentId() == null ? null : load(f.parentId());
        checkLevel(type, parent);
        if (codeExists(f.code(), null)) {
            throw new BusinessException("Location code already exists: " + f.code());
        }
        Location loc = repo.save(new Location(f.code(), f.name(), type, parent)); // IDENTITY: id assigned on insert
        loc.refreshPath();
        return LocationView.from(loc);
    }

    @Transactional
    public LocationView update(Long id, LocationForm f) {
        Location loc = load(id);
        loc.assertVersion(f.version());
        LocationType type = Objects.equals(loc.getType().getId(), f.typeId()) ? loc.getType() : activeType(f.typeId());
        Location parent = f.parentId() == null ? null : load(f.parentId());

        if (parent != null && HierarchyPath.isSelfOrDescendant(parent.getPath(), loc.getPath())) {
            throw new BusinessException("A location cannot be moved under itself or one of its children");
        }
        checkLevel(type, parent);
        Integer minChildLevel = repo.minChildLevel(id);
        if (minChildLevel != null && minChildLevel <= type.getLevelNo()) {
            throw new BusinessException("Type level must stay above the level of the existing child locations");
        }
        if (codeExists(f.code(), id)) {
            throw new BusinessException("Location code already exists: " + f.code());
        }

        String oldPath = loc.getPath();
        loc.change(f.code(), f.name(), type, parent, f.status());
        loc.refreshPath();
        String newPath = loc.getPath();
        if (!newPath.equals(oldPath)) {                    // moved: re-root every descendant
            for (Location d : repo.findSubtree(oldPath)) {
                if (!d.getId().equals(id)) {
                    d.rebasePath(oldPath, newPath);
                }
            }
        }
        return LocationView.from(loc);
    }

    @Transactional
    public void delete(Long id) {
        Location loc = load(id);
        if (repo.existsByParentId(id)) {
            throw new BusinessException("Location has child locations. Delete or move them first.");
        }
        repo.delete(loc);
        repo.flush();                                      // surface FK violations (employees, org units) here
    }

    /** For other corehr services (employee, organogram) that need to link to a location. */
    @Transactional(readOnly = true)
    public Location findActiveEntity(Long id) {
        Location loc = load(id);
        if (loc.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException("Location is inactive: " + loc.getCode());
        }
        return loc;
    }

    /* ------------------------------ helpers ------------------------------ */

    private Location load(Long id) {
        return repo.findWithType(id).orElseThrow(() -> new NotFoundException("Location", id));
    }

    private LocationType activeType(Long typeId) {
        LocationType type = typeService.findEntity(typeId);
        if (type.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException("Location type is inactive: " + type.getCode());
        }
        return type;
    }

    private static void checkLevel(LocationType type, Location parent) {
        String error = HierarchyPath.checkLevel(type.getLevelNo(),
                parent == null ? null : parent.getType().getLevelNo(), "location");
        if (error != null) {
            throw new BusinessException(error);
        }
    }
}
