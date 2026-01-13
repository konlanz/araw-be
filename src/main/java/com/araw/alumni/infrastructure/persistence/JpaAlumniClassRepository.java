package com.araw.alumni.infrastructure.persistence;

import com.araw.alumni.domain.model.AlumniClass;
import com.araw.alumni.domain.repository.AlumniClassRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaAlumniClassRepository extends AlumniClassRepository, JpaRepository<AlumniClass, UUID> {

    List<AlumniClass> findAllByOrderByGraduationYearDescNameAsc();

    @Override
    default List<AlumniClass> findAllOrderedByGraduationYearDesc() {
        return findAllByOrderByGraduationYearDescNameAsc();
    }
}
