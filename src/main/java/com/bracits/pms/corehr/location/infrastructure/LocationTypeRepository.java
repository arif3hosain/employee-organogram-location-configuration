package com.bracits.pms.corehr.location.infrastructure;

import com.bracits.pms.corehr.location.domain.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationTypeRepository extends JpaRepository<LocationType, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<LocationType> findAllByOrderByLevelNoAscNameAsc();
}
