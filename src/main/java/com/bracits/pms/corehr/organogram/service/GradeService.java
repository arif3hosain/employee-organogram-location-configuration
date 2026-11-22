package com.bracits.pms.corehr.organogram.service;

import com.bracits.pms.corehr.organogram.api.GradeForm;
import com.bracits.pms.corehr.organogram.api.GradeView;
import com.bracits.pms.corehr.organogram.domain.Grade;
import com.bracits.pms.corehr.organogram.infrastructure.DesignationRepository;
import com.bracits.pms.corehr.organogram.infrastructure.GradeRepository;
import com.bracits.pms.shared.domain.RecordStatus;
import com.bracits.pms.shared.error.BusinessException;
import com.bracits.pms.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository repo;
    private final DesignationRepository designationRepo;

    @Transactional(readOnly = true)
    public List<GradeView> list() {
        return repo.findAllByOrderByRankNoAsc().stream().map(GradeView::from).toList();
    }

    private boolean codeExists(String code, Long excludeId) {
        return excludeId == null
                ? repo.existsByCodeIgnoreCase(code.trim())
                : repo.existsByCodeIgnoreCaseAndIdNot(code.trim(), excludeId);
    }

    private boolean rankExists(Integer rankNo, Long excludeId) {
        return excludeId == null ? repo.existsByRankNo(rankNo) : repo.existsByRankNoAndIdNot(rankNo, excludeId);
    }

    @Transactional
    public GradeView create(GradeForm f) {
        checkUnique(f, null);
        return GradeView.from(repo.save(new Grade(f.code(), f.name(), f.rankNo())));
    }

    @Transactional
    public GradeView update(Long id, GradeForm f) {
        Grade g = findEntity(id);
        g.assertVersion(f.version());
        checkUnique(f, id);
        // Making a grade INACTIVE is allowed: designations already on it keep it,
        // only new assignments are blocked (findActiveEntity).
        g.change(f.code(), f.name(), f.rankNo(), f.status());
        return GradeView.from(g);
    }

    @Transactional
    public void delete(Long id) {
        Grade g = findEntity(id);
        if (designationRepo.existsByGradeId(id)) {
            throw new BusinessException("Grade is used by designations. Move them to another grade or make it INACTIVE.");
        }
        repo.delete(g);
    }

    private void checkUnique(GradeForm f, Long excludeId) {
        if (codeExists(f.code(), excludeId)) {
            throw new BusinessException("Grade code already exists: " + f.code());
        }
        if (rankExists(f.rankNo(), excludeId)) {
            throw new BusinessException("Another grade already has rank " + f.rankNo());
        }
    }

    @Transactional(readOnly = true)
    public Grade findEntity(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Grade", id));
    }

    /** For DesignationService: a grade that may be assigned. */
    @Transactional(readOnly = true)
    public Grade findActiveEntity(Long id) {
        Grade g = findEntity(id);
        if (g.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException("Grade is inactive: " + g.getCode());
        }
        return g;
    }
}
