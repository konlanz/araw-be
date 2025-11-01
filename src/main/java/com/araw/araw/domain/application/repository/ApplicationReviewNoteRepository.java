package com.araw.araw.domain.application.repository;

import com.araw.araw.domain.application.entity.ApplicationReviewNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationReviewNoteRepository extends JpaRepository<ApplicationReviewNote, UUID> {

    @Query("SELECT n FROM ApplicationReviewNote n WHERE n.application.id = :applicationId ORDER BY n.createdAt DESC")
    List<ApplicationReviewNote> findByApplicationIdOrderByCreatedAtDesc(@Param("applicationId") UUID applicationId);
}
