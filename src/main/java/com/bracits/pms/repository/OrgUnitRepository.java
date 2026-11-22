package com.bracits.pms.repository;

import com.bracits.pms.domain.OrgUnit;
import com.bracits.pms.domain.OrgUnitNature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrgUnitRepository extends JpaRepository<OrgUnit, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByParentId(Long parentId);

    boolean existsByTypeId(Long typeId);

    @Query("select u from OrgUnit u join fetch u.type left join fetch u.parent left join fetch u.location where u.id = :id")
    Optional<OrgUnit> findFull(@Param("id") Long id);

    @Query("""
            select u from OrgUnit u join fetch u.type t left join fetch u.parent p left join fetch u.location
            where (:typeId is null or t.id = :typeId)
              and (:parentId is null or p.id = :parentId)
              and (:nature is null or u.nature = :nature)
              and (lower(u.name) like :q or lower(u.code) like :q)
            order by t.levelNo, u.sortOrder, u.name
            """)
    List<OrgUnit> search(@Param("typeId") Long typeId, @Param("parentId") Long parentId,
                         @Param("nature") OrgUnitNature nature, @Param("q") String q);

    @Query("""
            select u from OrgUnit u join fetch u.type left join fetch u.parent left join fetch u.location
            where u.path like concat(:prefix, '%') order by u.path
            """)
    List<OrgUnit> findSubtree(@Param("prefix") String prefix);

    @Query("select u from OrgUnit u join fetch u.type left join fetch u.parent left join fetch u.location order by u.path")
    List<OrgUnit> findAllFull();

    @Query("select min(t.levelNo) from OrgUnit u join u.type t where u.parent.id = :parentId")
    Integer minChildLevel(@Param("parentId") Long parentId);
}
