package com.bracits.pms.service;

import com.bracits.pms.domain.Country;
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
    private final CountryService countryService;

    /** All location types, or one country's (countryId given). */
    @Transactional(readOnly = true)
    public List<LocationTypeView> list(Long countryId) {
        return typeRepo.list(countryId).stream().map(LocationTypeView::from).toList();
    }

    private boolean codeExists(Long countryId, String code, Long excludeId) {
        return excludeId == null
                ? typeRepo.existsByCountryIdAndCodeIgnoreCase(countryId, code.trim())
                : typeRepo.existsByCountryIdAndCodeIgnoreCaseAndIdNot(countryId, code.trim(), excludeId);
    }

    @Transactional
    public LocationTypeView create(LocationTypeForm f) {
        Country country = countryService.findActiveEntity(f.countryId());
        checkUniqueCode(country, f.code(), null);
        return LocationTypeView.from(typeRepo.save(new LocationType(country, f.code(), f.name(), f.levelNo())));
    }

    @Transactional
    public LocationTypeView update(Long id, LocationTypeForm f) {
        LocationType type = findEntity(id);
        type.assertVersion(f.version());
        boolean inUse = locationRepo.existsByTypeId(id);
        if (!Objects.equals(type.getCountry().getId(), f.countryId())) {
            if (inUse) {
                throw new BusinessException("Country cannot change while locations use this type");
            }
            type.moveToCountry(countryService.findActiveEntity(f.countryId()));
        }
        checkUniqueCode(type.getCountry(), f.code(), id);
        if (!Objects.equals(type.getLevelNo(), f.levelNo()) && inUse) {
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

    private void checkUniqueCode(Country country, String code, Long excludeId) {
        if (codeExists(country.getId(), code, excludeId)) {
            throw new BusinessException("Location type code already exists in " + country.getName() + ": " + code);
        }
    }

    /** For LocationService (same module). Public so the Spring proxy delegates it correctly. Country is loaded. */
    @Transactional(readOnly = true)
    public LocationType findEntity(Long id) {
        return typeRepo.findWithCountry(id).orElseThrow(() -> new NotFoundException("Location type", id));
    }
}
