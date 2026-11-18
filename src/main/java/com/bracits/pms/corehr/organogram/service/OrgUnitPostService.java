package com.bracits.pms.corehr.organogram.service;

import com.bracits.pms.corehr.organogram.api.OrgUnitPostForm;
import com.bracits.pms.corehr.organogram.api.OrgUnitPostView;
import com.bracits.pms.corehr.organogram.domain.Designation;
import com.bracits.pms.corehr.organogram.domain.OrgUnit;
import com.bracits.pms.corehr.organogram.domain.OrgUnitPost;
import com.bracits.pms.corehr.organogram.infrastructure.OrgUnitPostRepository;
import com.bracits.pms.shared.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrgUnitPostService {

    private final OrgUnitPostRepository repo;
    private final OrgUnitService orgUnitService;
    private final DesignationService designationService;

    public OrgUnitPostService(OrgUnitPostRepository repo, OrgUnitService orgUnitService,
                              DesignationService designationService) {
        this.repo = repo;
        this.orgUnitService = orgUnitService;
        this.designationService = designationService;
    }

    /** Sanctioned posts of one unit with filled and vacant counts. */
    @Transactional(readOnly = true)
    public List<OrgUnitPostView> listByOrgUnit(Long orgUnitId) {
        Map<Long, Integer> filled = filledByDesignation(orgUnitId);
        return repo.findByOrgUnit(orgUnitId).stream()
                .map(p -> toView(p, filled.getOrDefault(p.getDesignation().getId(), 0)))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return repo.existsById(id);
    }

    /** Create the post line, or change its sanctioned count if it already exists. */
    @Transactional
    public OrgUnitPostView save(OrgUnitPostForm f) {
        OrgUnit unit = orgUnitService.findActiveEntity(f.orgUnitId());
        Designation designation = designationService.findActiveEntity(f.designationId());
        OrgUnitPost post = repo.findByOrgUnitIdAndDesignationId(unit.getId(), designation.getId())
                .orElseGet(() -> new OrgUnitPost(unit, designation, f.sanctionedCount()));
        post.changeSanctionedCount(f.sanctionedCount());
        post = repo.save(post);
        return toView(post, filledByDesignation(unit.getId()).getOrDefault(designation.getId(), 0));
    }

    @Transactional
    public void delete(Long id) {
        OrgUnitPost post = repo.findById(id).orElseThrow(() -> new NotFoundException("Org unit post", id));
        repo.delete(post);
    }

    private Map<Long, Integer> filledByDesignation(Long orgUnitId) {
        Map<Long, Integer> filled = new HashMap<>();
        for (Object[] row : repo.countActiveEmployeesByDesignation(orgUnitId)) {
            filled.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue());
        }
        return filled;
    }

    private static OrgUnitPostView toView(OrgUnitPost p, int filled) {
        Designation d = p.getDesignation();
        return new OrgUnitPostView(p.getId(), p.getOrgUnit().getId(), d.getId(), d.getCode(), d.getName(),
                d.getGrade().getCode(), d.getGrade().getRankNo(), p.getSanctionedCount(), filled, p.getSanctionedCount() - filled);
    }
}
