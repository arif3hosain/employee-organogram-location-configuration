package com.bracits.pms.service;

import com.bracits.pms.domain.Country;
import com.bracits.pms.domain.RecordStatus;
import com.bracits.pms.dto.record.CountryForm;
import com.bracits.pms.dto.record.CountryView;
import com.bracits.pms.exception.BusinessException;
import com.bracits.pms.exception.NotFoundException;
import com.bracits.pms.repository.CountryRepository;
import com.bracits.pms.repository.LocationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository repo;
    private final LocationTypeRepository locationTypeRepo;

    @Transactional(readOnly = true)
    public List<CountryView> list() {
        return repo.findAllByOrderByNameAsc().stream().map(CountryView::from).toList();
    }

    @Transactional(readOnly = true)
    public CountryView get(Long id) {
        return CountryView.from(findEntity(id));
    }

    @Transactional
    public CountryView create(CountryForm f) {
        checkUnique(f, null);
        return CountryView.from(repo.save(new Country(f.iso2(), f.iso3(), f.name(), f.currencyCode(), f.phoneCode())));
    }

    @Transactional
    public CountryView update(Long id, CountryForm f) {
        Country c = findEntity(id);
        c.assertVersion(f.version());
        checkUnique(f, id);
        // Making a country INACTIVE is allowed: its existing types and locations stay,
        // only new types and locations are blocked (findActiveEntity).
        c.change(f.iso2(), f.iso3(), f.name(), f.currencyCode(), f.phoneCode(), f.status());
        return CountryView.from(c);
    }

    @Transactional
    public void delete(Long id) {
        Country c = findEntity(id);
        if (locationTypeRepo.existsByCountryId(id)) {
            throw new BusinessException("Country has location types. Delete them first or make the country INACTIVE.");
        }
        repo.delete(c);
    }

    private void checkUnique(CountryForm f, Long excludeId) {
        boolean iso2Taken = excludeId == null
                ? repo.existsByIso2IgnoreCase(f.iso2().trim())
                : repo.existsByIso2IgnoreCaseAndIdNot(f.iso2().trim(), excludeId);
        if (iso2Taken) {
            throw new BusinessException("Country ISO2 code already exists: " + f.iso2());
        }
        boolean iso3Taken = excludeId == null
                ? repo.existsByIso3IgnoreCase(f.iso3().trim())
                : repo.existsByIso3IgnoreCaseAndIdNot(f.iso3().trim(), excludeId);
        if (iso3Taken) {
            throw new BusinessException("Country ISO3 code already exists: " + f.iso3());
        }
    }

    @Transactional(readOnly = true)
    public Country findEntity(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Country", id));
    }

    /** For LocationTypeService: a country that may receive new location types. */
    @Transactional(readOnly = true)
    public Country findActiveEntity(Long id) {
        Country c = findEntity(id);
        if (c.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException("Country is inactive: " + c.getIso2());
        }
        return c;
    }
}
