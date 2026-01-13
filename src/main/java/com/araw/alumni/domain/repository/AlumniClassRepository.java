package com.araw.alumni.domain.repository;

import com.araw.alumni.domain.model.AlumniClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlumniClassRepository {

    AlumniClass save(AlumniClass alumniClass);

    Optional<AlumniClass> findById(UUID id);

    Page<AlumniClass> findAll(Pageable pageable);

    List<AlumniClass> findAllOrderedByGraduationYearDesc();

    void delete(AlumniClass alumniClass);
}
