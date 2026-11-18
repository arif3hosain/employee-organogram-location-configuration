package com.bracits.pms.corehr.employee.infrastructure;

import com.bracits.pms.corehr.employee.domain.Employee;
import com.bracits.pms.shared.domain.RecordStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmployeeCodeIgnoreCase(String code);

    boolean existsByEmployeeCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsBySupervisorId(Long supervisorId);

    @Query("""
            select e from Employee e
              join fetch e.designation d join fetch d.grade join fetch e.orgUnit join fetch e.location left join fetch e.supervisor
            where e.id = :id
            """)
    Optional<Employee> findFull(@Param("id") Long id);

    /**
     * qPattern, orgPattern and locationPattern are LIKE patterns built by the service and never null
     * ("%" = no filter). PostgreSQL cannot type a null parameter inside "like (? || '%')" (it guesses
     * bytea and fails), so string filters must not use the ":x is null or ..." idiom.
     * orgPattern / locationPattern filter the whole subtree, e.g. every employee anywhere under the
     * Technology function, or at any office anywhere in Dhaka division.
     */
    @Query(value = """
            select e from Employee e
              join fetch e.designation d join fetch d.grade join fetch e.orgUnit u join fetch e.location l left join fetch e.supervisor
            where (lower(e.fullName) like :qPattern or lower(e.employeeCode) like :qPattern or e.email like :qPattern)
              and u.path like :orgPattern
              and l.path like :locationPattern
              and (:designationId is null or d.id = :designationId)
              and (:status is null or e.status = :status)
            """,
            countQuery = """
            select count(e) from Employee e join e.designation d join e.orgUnit u join e.location l
            where (lower(e.fullName) like :qPattern or lower(e.employeeCode) like :qPattern or e.email like :qPattern)
              and u.path like :orgPattern
              and l.path like :locationPattern
              and (:designationId is null or d.id = :designationId)
              and (:status is null or e.status = :status)
            """)
    Page<Employee> search(@Param("qPattern") String qPattern, @Param("orgPattern") String orgPattern,
                          @Param("locationPattern") String locationPattern, @Param("designationId") Long designationId,
                          @Param("status") RecordStatus status, Pageable pageable);
}
