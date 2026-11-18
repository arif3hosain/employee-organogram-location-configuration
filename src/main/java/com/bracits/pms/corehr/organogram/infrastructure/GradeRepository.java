package com.bracits.pms.corehr.organogram.infrastructure;

import com.bracits.pms.corehr.organogram.domain.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByRankNo(Integer rankNo);

    boolean existsByRankNoAndIdNot(Integer rankNo, Long id);

    List<Grade> findAllByOrderByRankNoAsc();
}
