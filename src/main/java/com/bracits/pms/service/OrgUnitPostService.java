package com.bracits.pms.service;

import com.bracits.pms.domain.Designation;
import com.bracits.pms.domain.OrgUnit;
import com.bracits.pms.domain.OrgUnitPost;
import com.bracits.pms.dto.record.OrgUnitPostForm;
import com.bracits.pms.dto.record.OrgUnitPostView;
import com.bracits.pms.exception.NotFoundException;
import com.bracits.pms.repository.OrgUnitPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrgUnitPostService {

    private final OrgUnitPostRepository repo;
    private final OrgUnitService orgUnitService;
    private final DesignationService designationService;

    /** Sanctioned posts of one unit with filled and vacant counts. */
    @Transactional(readOnly = true)
    public List<OrgUnitPostView> listByOrgUnit(Long orgUnitId) {
        if (!orgUnitService.exists(orgUnitId)) {
            throw new NotFoundException("Org unit", orgUnitId);   // -> 404
        }
        Map<Long, Integer> filled = filledByDesignation(orgUnitId);
        return repo.findByOrgUnit(orgUnitId).stream()
                .map(p -> toView(p, filled.getOrDefault(p.getDesignation().getId(), 0)))
                .toList();
    }

    /** Create the post line, or change its sanctioned count if it already exists. */
    @Transactional
    public OrgUnitPostView save(Long orgUnitId, OrgUnitPostForm f) {
        OrgUnit unit = orgUnitService.findActiveEntity(orgUnitId);
        Designation designation = designationService.findActiveEntity(f.designationId());
        OrgUnitPost post = repo.findByOrgUnitIdAndDesignationId(unit.getId(), designation.getId())
                .orElseGet(() -> new OrgUnitPost(unit, designation, f.sanctionedCount()));
        post.changeSanctionedCount(f.sanctionedCount());
        post = repo.save(post);
        OrgUnitPostView view = toView(post, filledByDesignation(unit.getId()).getOrDefault(designation.getId(), 0));
        if (view.vacancy() < 0) {
            log.warn("Org unit {} is over-staffed for {} by {}", view.orgUnitId(), view.designationCode(), -view.vacancy());
        }
        return view;
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
