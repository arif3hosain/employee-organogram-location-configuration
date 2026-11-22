package com.bracits.pms.repository;

import com.bracits.pms.domain.OrgUnitPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrgUnitPostRepository extends JpaRepository<OrgUnitPost, Long> {

    @Query("""
            select p from OrgUnitPost p join fetch p.designation d join fetch d.grade g join fetch p.orgUnit u
            where u.id = :orgUnitId order by g.rankNo, d.name
            """)
    List<OrgUnitPost> findByOrgUnit(@Param("orgUnitId") Long orgUnitId);

    Optional<OrgUnitPost> findByOrgUnitIdAndDesignationId(Long orgUnitId, Long designationId);

    boolean existsByDesignationId(Long designationId);

    /**
     * Filled positions per designation in one unit. Native SQL on purpose: it reads the EMPLOYEE
     * table without importing the employee package, so organogram does not depend on employee
     * (employee depends on organogram, not the other way round).
     * Returns rows of [designation_id, count].
     */
    @Query(value = """
            select designation_id, count(*) from employee
            where org_unit_id = :orgUnitId and status = 'ACTIVE'
            group by designation_id
            """, nativeQuery = true)
    List<Object[]> countActiveEmployeesByDesignation(@Param("orgUnitId") Long orgUnitId);
}
