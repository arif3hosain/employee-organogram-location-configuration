package com.bracits.pms.repository;

import com.bracits.pms.domain.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationTypeRepository extends JpaRepository<LocationType, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<LocationType> findAllByOrderByLevelNoAscNameAsc();
}
