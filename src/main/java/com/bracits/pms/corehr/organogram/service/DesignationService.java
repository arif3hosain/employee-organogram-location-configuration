package com.bracits.pms.corehr.organogram.service;

import com.bracits.pms.corehr.organogram.api.DesignationForm;
import com.bracits.pms.corehr.organogram.api.DesignationView;
import com.bracits.pms.corehr.organogram.domain.Designation;
import com.bracits.pms.corehr.organogram.domain.Grade;
import com.bracits.pms.corehr.organogram.infrastructure.DesignationRepository;
import com.bracits.pms.corehr.organogram.infrastructure.OrgUnitPostRepository;
import com.bracits.pms.shared.domain.RecordStatus;
import com.bracits.pms.shared.error.BusinessException;
import com.bracits.pms.shared.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class DesignationService {

    private final DesignationRepository repo;
    private final OrgUnitPostRepository postRepo;
    private final GradeService gradeService;

    public DesignationService(DesignationRepository repo, OrgUnitPostRepository postRepo, GradeService gradeService) {
        this.repo = repo;
        this.postRepo = postRepo;
        this.gradeService = gradeService;
    }

    /** Most senior first (by grade rank), then by name. */
    @Transactional(readOnly = true)
    public List<DesignationView> list() {
        return repo.findAllWithGrade().stream().map(DesignationView::from).toList();
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

    @Transactional
    public DesignationView save(DesignationForm f) {
        if (codeExists(f.code(), f.id())) {
            throw new BusinessException("Designation code already exists: " + f.code());
        }
        Designation d;
        if (f.id() == null) {
            d = new Designation(f.code(), f.name(), gradeService.findActiveEntity(f.gradeId()));
        } else {
            d = findEntity(f.id());
            d.assertVersion(f.version());
            // keep the current grade even if it was made INACTIVE later; a new choice must be active
            Grade grade = Objects.equals(d.getGrade().getId(), f.gradeId())
                    ? d.getGrade() : gradeService.findActiveEntity(f.gradeId());
            d.change(f.code(), f.name(), grade, f.status());
        }
        return DesignationView.from(repo.save(d));
    }

    @Transactional
    public void delete(Long id) {
        Designation d = findEntity(id);
        if (postRepo.existsByDesignationId(id)) {
            throw new BusinessException("Designation is used in sanctioned posts. Remove those first or make it INACTIVE.");
        }
        repo.delete(d);
        repo.flush();                                      // surface FK violations (employees) here
    }

    @Transactional(readOnly = true)
    public Designation findEntity(Long id) {
        return repo.findWithGrade(id).orElseThrow(() -> new NotFoundException("Designation", id));
    }

    /** For the employee service: a designation that may be assigned. */
    @Transactional(readOnly = true)
    public Designation findActiveEntity(Long id) {
        Designation d = findEntity(id);
        if (d.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException("Designation is inactive: " + d.getCode());
        }
        return d;
    }
}
