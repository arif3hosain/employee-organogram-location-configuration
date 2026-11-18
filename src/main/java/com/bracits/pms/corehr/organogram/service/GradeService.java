package com.bracits.pms.corehr.organogram.service;

import com.bracits.pms.corehr.organogram.api.GradeForm;
import com.bracits.pms.corehr.organogram.api.GradeView;
import com.bracits.pms.corehr.organogram.domain.Grade;
import com.bracits.pms.corehr.organogram.infrastructure.DesignationRepository;
import com.bracits.pms.corehr.organogram.infrastructure.GradeRepository;
import com.bracits.pms.shared.domain.RecordStatus;
import com.bracits.pms.shared.error.BusinessException;
import com.bracits.pms.shared.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GradeService {

    private final GradeRepository repo;
    private final DesignationRepository designationRepo;

    public GradeService(GradeRepository repo, DesignationRepository designationRepo) {
        this.repo = repo;
        this.designationRepo = designationRepo;
    }

    @Transactional(readOnly = true)
    public List<GradeView> list() {
        return repo.findAllByOrderByRankNoAsc().stream().map(GradeView::from).toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return repo.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean codeExists(String code, Long excludeId) {
        return excludeId == null
                ? repo.existsByCodeIgnoreCase(code.trim())
                : repo.existsByCodeIgnoreCaseAndIdNot(code.trim(), excludeId);
    }

    @Transactional(readOnly = true)
    public boolean rankExists(Integer rankNo, Long excludeId) {
        return excludeId == null ? repo.existsByRankNo(rankNo) : repo.existsByRankNoAndIdNot(rankNo, excludeId);
    }

    @Transactional
    public GradeView save(GradeForm f) {
        if (codeExists(f.code(), f.id())) {
            throw new BusinessException("Grade code already exists: " + f.code());
        }
        if (rankExists(f.rankNo(), f.id())) {
            throw new BusinessException("Another grade already has rank " + f.rankNo());
        }
        Grade g;
        if (f.id() == null) {
            g = new Grade(f.code(), f.name(), f.rankNo());
        } else {
            g = findEntity(f.id());
            g.assertVersion(f.version());
            // Making a grade INACTIVE is allowed: designations already on it keep it,
            // only new assignments are blocked (findActiveEntity).
            g.change(f.code(), f.name(), f.rankNo(), f.status());
        }
        return GradeView.from(repo.save(g));
    }

    @Transactional
    public void delete(Long id) {
        Grade g = findEntity(id);
        if (designationRepo.existsByGradeId(id)) {
            throw new BusinessException("Grade is used by designations. Move them to another grade or make it INACTIVE.");
        }
        repo.delete(g);
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
