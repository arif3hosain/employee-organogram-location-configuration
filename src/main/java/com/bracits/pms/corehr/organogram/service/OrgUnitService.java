package com.bracits.pms.corehr.organogram.service;

import com.bracits.pms.corehr.location.domain.Location;
import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.corehr.organogram.api.OrgUnitForm;
import com.bracits.pms.corehr.organogram.api.OrgUnitNode;
import com.bracits.pms.corehr.organogram.api.OrgUnitSearch;
import com.bracits.pms.corehr.organogram.api.OrgUnitView;
import com.bracits.pms.corehr.organogram.domain.OrgUnit;
import com.bracits.pms.corehr.organogram.domain.OrgUnitType;
import com.bracits.pms.corehr.organogram.infrastructure.OrgUnitRepository;
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
public class OrgUnitService {

    private final OrgUnitRepository repo;
    private final OrgUnitTypeService typeService;
    private final LocationService locationService;      // other sub-module: called through its service only

    /* ------------------------------ queries ------------------------------ */

    @Transactional(readOnly = true)
    public OrgUnitView get(Long id) {
        return OrgUnitView.from(load(id));
    }

    @Transactional(readOnly = true)
    public List<OrgUnitView> search(OrgUnitSearch s) {
        String q = s.q() == null || s.q().isBlank() ? "%" : "%" + s.q().trim().toLowerCase() + "%";   // never null (PostgreSQL)
        return repo.search(s.typeId(), s.parentId(), s.nature(), q).stream().map(OrgUnitView::from).toList();
    }

    /** The organogram chart: whole tree, or the subtree under rootId. */
    @Transactional(readOnly = true)
    public List<OrgUnitNode> tree(Long rootId) {
        List<OrgUnit> rows = rootId == null ? repo.findAllFull() : repo.findSubtree(load(rootId).getPath());
        Map<Long, OrgUnitNode> byId = new LinkedHashMap<>();
        List<OrgUnitNode> roots = new ArrayList<>();
        for (OrgUnit u : rows) {                           // ordered by path, so parents come first
            OrgUnitNode node = OrgUnitNode.of(OrgUnitView.from(u));
            byId.put(u.getId(), node);
            OrgUnitNode parent = u.getParent() == null ? null : byId.get(u.getParent().getId());
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
    public OrgUnitView create(OrgUnitForm f) {
        OrgUnitType type = activeType(f.typeId());
        OrgUnit parent = f.parentId() == null ? null : load(f.parentId());
        Location location = f.locationId() == null ? null : locationService.findActiveEntity(f.locationId());
        checkLevel(type, parent);
        if (codeExists(f.code(), null)) {
            throw new BusinessException("Org unit code already exists: " + f.code());
        }
        OrgUnit unit = repo.save(new OrgUnit(f.code(), f.name(), type, f.nature(), parent, location, f.sortOrder()));
        unit.refreshPath();
        return OrgUnitView.from(unit);
    }

    @Transactional
    public OrgUnitView update(Long id, OrgUnitForm f) {
        OrgUnit unit = load(id);
        unit.assertVersion(f.version());
        OrgUnitType type = Objects.equals(unit.getType().getId(), f.typeId()) ? unit.getType() : activeType(f.typeId());
        OrgUnit parent = f.parentId() == null ? null : load(f.parentId());
        Location location = f.locationId() == null ? null
                : (unit.getLocation() != null && unit.getLocation().getId().equals(f.locationId())
                   ? unit.getLocation() : locationService.findActiveEntity(f.locationId()));

        if (parent != null && HierarchyPath.isSelfOrDescendant(parent.getPath(), unit.getPath())) {
            throw new BusinessException("An org unit cannot be moved under itself or one of its children");
        }
        checkLevel(type, parent);
        Integer minChildLevel = repo.minChildLevel(id);
        if (minChildLevel != null && minChildLevel <= type.getLevelNo()) {
            throw new BusinessException("Type level must stay above the level of the existing child units");
        }
        if (codeExists(f.code(), id)) {
            throw new BusinessException("Org unit code already exists: " + f.code());
        }

        String oldPath = unit.getPath();
        unit.change(f.code(), f.name(), type, f.nature(), parent, location, f.sortOrder(), f.status());
        unit.refreshPath();
        String newPath = unit.getPath();
        if (!newPath.equals(oldPath)) {                    // moved: re-root every descendant
            for (OrgUnit d : repo.findSubtree(oldPath)) {
                if (!d.getId().equals(id)) {
                    d.rebasePath(oldPath, newPath);
                }
            }
        }
        return OrgUnitView.from(unit);
    }

    @Transactional
    public void delete(Long id) {
        OrgUnit unit = load(id);
        if (repo.existsByParentId(id)) {
            throw new BusinessException("Org unit has child units. Delete or move them first.");
        }
        repo.delete(unit);
        repo.flush();                                      // surface FK violations (posts, employees) here
    }

    /** For the employee service and the post service. */
    @Transactional(readOnly = true)
    public OrgUnit findActiveEntity(Long id) {
        OrgUnit unit = load(id);
        if (unit.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException("Org unit is inactive: " + unit.getCode());
        }
        return unit;
    }

    /* ------------------------------ helpers ------------------------------ */

    private OrgUnit load(Long id) {
        return repo.findFull(id).orElseThrow(() -> new NotFoundException("Org unit", id));
    }

    private OrgUnitType activeType(Long typeId) {
        OrgUnitType type = typeService.findEntity(typeId);
        if (type.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException("Org unit type is inactive: " + type.getCode());
        }
        return type;
    }

    private static void checkLevel(OrgUnitType type, OrgUnit parent) {
        String error = HierarchyPath.checkLevel(type.getLevelNo(),
                parent == null ? null : parent.getType().getLevelNo(), "org unit");
        if (error != null) {
            throw new BusinessException(error);
        }
    }
}
