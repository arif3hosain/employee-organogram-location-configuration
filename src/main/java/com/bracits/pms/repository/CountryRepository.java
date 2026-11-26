package com.bracits.pms.repository;

import com.bracits.pms.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, Long> {

    boolean existsByIso2IgnoreCase(String iso2);

    boolean existsByIso2IgnoreCaseAndIdNot(String iso2, Long id);

    boolean existsByIso3IgnoreCase(String iso3);

    boolean existsByIso3IgnoreCaseAndIdNot(String iso3, Long id);

    List<Country> findAllByOrderByNameAsc();
}
