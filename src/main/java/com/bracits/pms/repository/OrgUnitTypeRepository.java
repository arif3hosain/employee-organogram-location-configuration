package com.bracits.pms.repository;

import com.bracits.pms.domain.OrgUnitType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgUnitTypeRepository extends JpaRepository<OrgUnitType, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<OrgUnitType> findAllByOrderByLevelNoAscNameAsc();
}
