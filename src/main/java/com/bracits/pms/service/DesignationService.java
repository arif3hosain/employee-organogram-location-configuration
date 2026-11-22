package com.bracits.pms.service;

import com.bracits.pms.domain.Designation;
import com.bracits.pms.domain.Grade;
import com.bracits.pms.domain.RecordStatus;
import com.bracits.pms.dto.record.DesignationForm;
import com.bracits.pms.dto.record.DesignationView;
import com.bracits.pms.exception.BusinessException;
import com.bracits.pms.exception.NotFoundException;
import com.bracits.pms.repository.DesignationRepository;
import com.bracits.pms.repository.OrgUnitPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DesignationService {

    private final DesignationRepository repo;
    private final OrgUnitPostRepository postRepo;
    private final GradeService gradeService;

    /** Most senior first (by grade rank), then by name. */
    @Transactional(readOnly = true)
    public List<DesignationView> list() {
        return repo.findAllWithGrade().stream().map(DesignationView::from).toList();
    }

    private boolean codeExists(String code, Long excludeId) {
        return excludeId == null
                ? repo.existsByCodeIgnoreCase(code.trim())
                : repo.existsByCodeIgnoreCaseAndIdNot(code.trim(), excludeId);
    }

    @Transactional
    public DesignationView create(DesignationForm f) {
        checkUniqueCode(f.code(), null);
        Designation d = new Designation(f.code(), f.name(), gradeService.findActiveEntity(f.gradeId()));
        return DesignationView.from(repo.save(d));
    }

    @Transactional
    public DesignationView update(Long id, DesignationForm f) {
        Designation d = findEntity(id);
        d.assertVersion(f.version());
        checkUniqueCode(f.code(), id);
        // keep the current grade even if it was made INACTIVE later; a new choice must be active
        Grade grade = Objects.equals(d.getGrade().getId(), f.gradeId())
                ? d.getGrade() : gradeService.findActiveEntity(f.gradeId());
        d.change(f.code(), f.name(), grade, f.status());
        return DesignationView.from(d);
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

    private void checkUniqueCode(String code, Long excludeId) {
        if (codeExists(code, excludeId)) {
            throw new BusinessException("Designation code already exists: " + code);
        }
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
