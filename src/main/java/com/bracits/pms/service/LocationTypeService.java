package com.bracits.pms.service;

import com.bracits.pms.domain.LocationType;
import com.bracits.pms.dto.record.LocationTypeForm;
import com.bracits.pms.dto.record.LocationTypeView;
import com.bracits.pms.exception.BusinessException;
import com.bracits.pms.exception.NotFoundException;
import com.bracits.pms.repository.LocationRepository;
import com.bracits.pms.repository.LocationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LocationTypeService {

    private final LocationTypeRepository typeRepo;
    private final LocationRepository locationRepo;

    @Transactional(readOnly = true)
    public List<LocationTypeView> list() {
        return typeRepo.findAllByOrderByLevelNoAscNameAsc().stream().map(LocationTypeView::from).toList();
    }

    private boolean codeExists(String code, Long excludeId) {
        return excludeId == null
                ? typeRepo.existsByCodeIgnoreCase(code.trim())
                : typeRepo.existsByCodeIgnoreCaseAndIdNot(code.trim(), excludeId);
    }

    @Transactional
    public LocationTypeView create(LocationTypeForm f) {
        checkUniqueCode(f.code(), null);
        return LocationTypeView.from(typeRepo.save(new LocationType(f.code(), f.name(), f.levelNo())));
    }

    @Transactional
    public LocationTypeView update(Long id, LocationTypeForm f) {
        LocationType type = findEntity(id);
        type.assertVersion(f.version());
        checkUniqueCode(f.code(), id);
        if (!Objects.equals(type.getLevelNo(), f.levelNo()) && locationRepo.existsByTypeId(id)) {
            throw new BusinessException("Level cannot change while locations use this type");
        }
        type.change(f.code(), f.name(), f.levelNo(), f.status());
        return LocationTypeView.from(type);
    }

    @Transactional
    public void delete(Long id) {
        LocationType type = findEntity(id);
        if (locationRepo.existsByTypeId(id)) {
            throw new BusinessException("Location type is in use and cannot be deleted. Make it INACTIVE instead.");
        }
        typeRepo.delete(type);
    }

    private void checkUniqueCode(String code, Long excludeId) {
        if (codeExists(code, excludeId)) {
            throw new BusinessException("Location type code already exists: " + code);
        }
    }

    /** For LocationService (same module). Public so the Spring proxy delegates it correctly. */
    @Transactional(readOnly = true)
    public LocationType findEntity(Long id) {
        return typeRepo.findById(id).orElseThrow(() -> new NotFoundException("Location type", id));
    }
}
