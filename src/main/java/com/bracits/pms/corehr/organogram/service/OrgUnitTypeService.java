package com.bracits.pms.corehr.organogram.service;

import com.bracits.pms.corehr.organogram.api.OrgUnitTypeForm;
import com.bracits.pms.corehr.organogram.api.OrgUnitTypeView;
import com.bracits.pms.corehr.organogram.domain.OrgUnitType;
import com.bracits.pms.corehr.organogram.infrastructure.OrgUnitRepository;
import com.bracits.pms.corehr.organogram.infrastructure.OrgUnitTypeRepository;
import com.bracits.pms.shared.error.BusinessException;
import com.bracits.pms.shared.error.NotFoundException;
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

    @Transactional(readOnly = true)
    public boolean codeExists(String code, Long excludeId) {
        return excludeId == null
                ? typeRepo.existsByCodeIgnoreCase(code.trim())
                : typeRepo.existsByCodeIgnoreCaseAndIdNot(code.trim(), excludeId);
    }

    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return typeRepo.existsById(id);
    }

    @Transactional
    public OrgUnitTypeView save(OrgUnitTypeForm f) {
        if (codeExists(f.code(), f.id())) {
            throw new BusinessException("Org unit type code already exists: " + f.code());
        }
        OrgUnitType type;
        if (f.id() == null) {
            type = new OrgUnitType(f.code(), f.name(), f.levelNo());
        } else {
            type = findEntity(f.id());
            type.assertVersion(f.version());
            if (!Objects.equals(type.getLevelNo(), f.levelNo()) && unitRepo.existsByTypeId(type.getId())) {
                throw new BusinessException("Level cannot change while org units use this type");
            }
            type.change(f.code(), f.name(), f.levelNo(), f.status());
        }
        return OrgUnitTypeView.from(typeRepo.save(type));
    }

    @Transactional
    public void delete(Long id) {
        OrgUnitType type = findEntity(id);
        if (unitRepo.existsByTypeId(id)) {
            throw new BusinessException("Org unit type is in use and cannot be deleted. Make it INACTIVE instead.");
        }
        typeRepo.delete(type);
    }

    @Transactional(readOnly = true)
    public OrgUnitType findEntity(Long id) {
        return typeRepo.findById(id).orElseThrow(() -> new NotFoundException("Org unit type", id));
    }
}
