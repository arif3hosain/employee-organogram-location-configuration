package com.bracits.pms.repository;

import com.bracits.pms.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByParentId(Long parentId);

    boolean existsByTypeId(Long typeId);

    @Query("select l from Location l join fetch l.type t join fetch t.country left join fetch l.parent where l.id = :id")
    Optional<Location> findWithType(@Param("id") Long id);

    @Query("""
            select l from Location l join fetch l.type t join fetch t.country c left join fetch l.parent p
            where (:countryId is null or c.id = :countryId)
              and (:typeId is null or t.id = :typeId)
              and (:parentId is null or p.id = :parentId)
              and (lower(l.name) like :q or lower(l.code) like :q)
            order by t.levelNo, l.name
            """)
    List<Location> search(@Param("countryId") Long countryId, @Param("typeId") Long typeId,
                          @Param("parentId") Long parentId, @Param("q") String q);

    @Query("select l from Location l join fetch l.type t join fetch t.country left join fetch l.parent where l.path like concat(:prefix, '%') order by l.path")
    List<Location> findSubtree(@Param("prefix") String prefix);

    /** Whole tree (countryId null) or one country's tree, ordered by path so parents come first. */
    @Query("""
            select l from Location l join fetch l.type t join fetch t.country c left join fetch l.parent
            where (:countryId is null or c.id = :countryId)
            order by l.path
            """)
    List<Location> findAllWithType(@Param("countryId") Long countryId);

    @Query("select min(t.levelNo) from Location l join l.type t where l.parent.id = :parentId")
    Integer minChildLevel(@Param("parentId") Long parentId);
}
