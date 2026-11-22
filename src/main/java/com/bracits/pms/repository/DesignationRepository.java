package com.bracits.pms.repository;

import com.bracits.pms.domain.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DesignationRepository extends JpaRepository<Designation, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByGradeId(Long gradeId);

    /** Most senior first (by grade), then by name. */
    @Query("select d from Designation d join fetch d.grade g order by g.rankNo, d.name")
    List<Designation> findAllWithGrade();

    @Query("select d from Designation d join fetch d.grade where d.id = :id")
    Optional<Designation> findWithGrade(@Param("id") Long id);
}
