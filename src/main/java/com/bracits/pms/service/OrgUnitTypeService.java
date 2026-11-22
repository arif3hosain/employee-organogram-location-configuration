package com.bracits.pms.service;

import com.bracits.pms.domain.OrgUnitType;
import com.bracits.pms.dto.record.OrgUnitTypeForm;
import com.bracits.pms.dto.record.OrgUnitTypeView;
import com.bracits.pms.exception.BusinessException;
import com.bracits.pms.exception.NotFoundException;
import com.bracits.pms.repository.OrgUnitRepository;
import com.bracits.pms.repository.OrgUnitTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrgUnitTypeService {

    private final OrgUnitTypeRepository typeRepo;
    private final OrgUnitRepository unitRepo;

    @Transactional(readOnly = true)
    public List<OrgUnitTypeView> list() {
        return typeRepo.findAllByOrderByLevelNoAscNameAsc().stream().map(OrgUnitTypeView::from).toList();
    }

    private boolean codeExists(String code, Long excludeId) {
        return excludeId == null
                ? typeRepo.existsByCodeIgnoreCase(code.trim())
                : typeRepo.existsByCodeIgnoreCaseAndIdNot(code.trim(), excludeId);
    }

    @Transactional
    public OrgUnitTypeView create(OrgUnitTypeForm f) {
        checkUniqueCode(f.code(), null);
        return OrgUnitTypeView.from(typeRepo.save(new OrgUnitType(f.code(), f.name(), f.levelNo())));
    }

    @Transactional
    public OrgUnitTypeView update(Long id, OrgUnitTypeForm f) {
        OrgUnitType type = findEntity(id);
        type.assertVersion(f.version());
        checkUniqueCode(f.code(), id);
        if (!Objects.equals(type.getLevelNo(), f.levelNo()) && unitRepo.existsByTypeId(id)) {
            throw new BusinessException("Level cannot change while org units use this type");
        }
        type.change(f.code(), f.name(), f.levelNo(), f.status());
        return OrgUnitTypeView.from(type);
    }

    @Transactional
    public void delete(Long id) {
        OrgUnitType type = findEntity(id);
        if (unitRepo.existsByTypeId(id)) {
            throw new BusinessException("Org unit type is in use and cannot be deleted. Make it INACTIVE instead.");
        }
        typeRepo.delete(type);
    }

    private void checkUniqueCode(String code, Long excludeId) {
        if (codeExists(code, excludeId)) {
            throw new BusinessException("Org unit type code already exists: " + code);
        }
    }

    @Transactional(readOnly = true)
    public OrgUnitType findEntity(Long id) {
        return typeRepo.findById(id).orElseThrow(() -> new NotFoundException("Org unit type", id));
    }
}
