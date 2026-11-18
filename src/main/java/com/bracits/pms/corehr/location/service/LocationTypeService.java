package com.bracits.pms.corehr.location.service;

import com.bracits.pms.corehr.location.api.LocationTypeForm;
import com.bracits.pms.corehr.location.api.LocationTypeView;
import com.bracits.pms.corehr.location.domain.LocationType;
import com.bracits.pms.corehr.location.infrastructure.LocationRepository;
import com.bracits.pms.corehr.location.infrastructure.LocationTypeRepository;
import com.bracits.pms.shared.error.BusinessException;
import com.bracits.pms.shared.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class LocationTypeService {

    private final LocationTypeRepository typeRepo;
    private final LocationRepository locationRepo;

    public LocationTypeService(LocationTypeRepository typeRepo, LocationRepository locationRepo) {
        this.typeRepo = typeRepo;
        this.locationRepo = locationRepo;
    }

    @Transactional(readOnly = true)
    public List<LocationTypeView> list() {
        return typeRepo.findAllByOrderByLevelNoAscNameAsc().stream().map(LocationTypeView::from).toList();
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

    /** Create when form.id is null, otherwise update. */
    @Transactional
    public LocationTypeView save(LocationTypeForm f) {
        if (codeExists(f.code(), f.id())) {
            throw new BusinessException("Location type code already exists: " + f.code());
        }
        LocationType type;
        if (f.id() == null) {
            type = new LocationType(f.code(), f.name(), f.levelNo());
        } else {
            type = findEntity(f.id());
            type.assertVersion(f.version());
            if (!Objects.equals(type.getLevelNo(), f.levelNo()) && locationRepo.existsByTypeId(type.getId())) {
                throw new BusinessException("Level cannot change while locations use this type");
            }
            type.change(f.code(), f.name(), f.levelNo(), f.status());
        }
        return LocationTypeView.from(typeRepo.save(type));
    }

    @Transactional
    public void delete(Long id) {
        LocationType type = findEntity(id);
        if (locationRepo.existsByTypeId(id)) {
            throw new BusinessException("Location type is in use and cannot be deleted. Make it INACTIVE instead.");
        }
        typeRepo.delete(type);
    }

    /** For LocationService (same module). Public so the Spring proxy delegates it correctly. */
    @Transactional(readOnly = true)
    public LocationType findEntity(Long id) {
        return typeRepo.findById(id).orElseThrow(() -> new NotFoundException("Location type", id));
    }
}
