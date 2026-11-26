package com.bracits.pms.repository;

import com.bracits.pms.domain.OrgUnitPost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * OrgUnitPost is a tenant CHILD table (no tenant_id; it belongs to its org unit's tenant).
 * This repository therefore extends plain {@link Repository}, not JpaRepository: there is no
 * unscoped findById/findAll. Every lookup goes through the org unit (whose queries Hibernate already
 * filters by tenant) or joins it explicitly.
 */
public interface OrgUnitPostRepository extends Repository<OrgUnitPost, Long> {

    OrgUnitPost save(OrgUnitPost post);

    void delete(OrgUnitPost post);

    /** One post, only if its org unit belongs to the given tenant. */
    @Query("select p from OrgUnitPost p join p.orgUnit u where p.id = :id and u.tenantId = :tenantId")
    Optional<OrgUnitPost> findInTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

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
     * Native SQL is not tenant-filtered by Hibernate; the tenant condition is written out here.
     * Returns rows of [designation_id, count].
     */
    @Query(value = """
            select designation_id, count(*) from employee
            where org_unit_id = :orgUnitId and tenant_id = :tenantId and status = 'ACTIVE'
            group by designation_id
            """, nativeQuery = true)
    List<Object[]> countActiveEmployeesByDesignation(@Param("orgUnitId") Long orgUnitId, @Param("tenantId") Long tenantId);
}
