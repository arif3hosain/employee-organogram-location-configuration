package com.bracits.pms.repository;

import com.bracits.pms.domain.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocationTypeRepository extends JpaRepository<LocationType, Long> {

    /** Type codes are unique per country. */
    boolean existsByCountryIdAndCodeIgnoreCase(Long countryId, String code);

    boolean existsByCountryIdAndCodeIgnoreCaseAndIdNot(Long countryId, String code, Long id);

    boolean existsByCountryId(Long countryId);

    @Query("select t from LocationType t join fetch t.country c where t.id = :id")
    Optional<LocationType> findWithCountry(@Param("id") Long id);

    /** All types (countryId null) or one country's, ordered by country, then level. */
    @Query("""
            select t from LocationType t join fetch t.country c
            where (:countryId is null or c.id = :countryId)
            order by c.name, t.levelNo, t.name
            """)
    List<LocationType> list(@Param("countryId") Long countryId);
}
