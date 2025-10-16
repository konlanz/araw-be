package com.araw.araw.domain.application.repository;

import com.araw.araw.domain.application.entity.Application;
import com.araw.araw.domain.application.valueobject.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Optional<Application> findByApplicationNumber(String applicationNumber);

    Optional<Application> findByConfirmationToken(String confirmationToken);

    List<Application> findByEventId(UUID eventId);

    Page<Application> findByEventId(UUID eventId, Pageable pageable);

    List<Application> findByEventIdAndStatus(UUID eventId, ApplicationStatus status);

    long countByEventId(UUID eventId);

    long countByEventIdAndStatus(UUID eventId, ApplicationStatus status);

    List<Application> findByParticipantId(UUID participantId);

    List<Application> findByParticipantIdOrderBySubmittedAtDesc(UUID participantId);

    List<Application> findByEmail(String email);

    List<Application> findByEmailOrderBySubmittedAtDesc(String email);

    boolean existsByEventIdAndEmail(UUID eventId, String email);

    boolean existsByEventIdAndParticipantId(UUID eventId, UUID participantId);

    List<Application> findByStatus(ApplicationStatus status);

    Page<Application> findByStatus(ApplicationStatus status, Pageable pageable);

    List<Application> findByStatusIn(List<ApplicationStatus> statuses);

    List<Application> findByStatusAndReviewedByIsNull(ApplicationStatus status);

    @Query("SELECT a FROM Application a WHERE a.status = 'SUBMITTED' " +
            "AND a.reviewedAt IS NULL ORDER BY a.submittedAt ASC")
    List<Application> findPendingReview();

    @Query("SELECT a FROM Application a WHERE a.reviewedBy = :reviewerName " +
            "AND a.reviewedAt BETWEEN :startDate AND :endDate")
    List<Application> findApplicationsReviewedBy(@Param("reviewerName") String reviewerName,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Application a WHERE a.status = 'WAITLISTED' " +
            "AND a.event.id = :eventId ORDER BY a.waitlistPosition ASC")
    List<Application> findWaitlistedByEventOrderByPosition(@Param("eventId") UUID eventId);

    @Query("SELECT MAX(a.waitlistPosition) FROM Application a " +
            "WHERE a.event.id = :eventId AND a.status = 'WAITLISTED'")
    Integer findMaxWaitlistPosition(@Param("eventId") UUID eventId);

    @Query("SELECT a FROM Application a WHERE a.guardianConsent = false " +
            "AND a.status = 'ACCEPTED'")
    List<Application> findAcceptedWithoutGuardianConsent();

    @Query("SELECT a FROM Application a WHERE a.submittedAt >= :date")
    List<Application> findApplicationsSubmittedAfter(@Param("date") LocalDateTime date);

    @Query("SELECT a FROM Application a WHERE a.submittedAt BETWEEN :startDate AND :endDate")
    List<Application> findApplicationsSubmittedBetween(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Application a WHERE a.status = 'ACCEPTED' " +
            "AND a.acceptanceSentAt IS NOT NULL " +
            "AND a.confirmedAt IS NULL " +
            "AND a.acceptanceSentAt < :deadline")
    List<Application> findUnconfirmedApplicationsPastDeadline(@Param("deadline") LocalDateTime deadline);

    @Query("SELECT a FROM Application a WHERE " +
            "LOWER(a.applicantInfo.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.applicantInfo.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.applicationNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Application> searchApplications(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT a FROM Application a WHERE " +
            "(:eventId IS NULL OR a.event.id = :eventId) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:email IS NULL OR a.email = :email) AND " +
            "(:hasGuardianConsent IS NULL OR a.guardianConsent = :hasGuardianConsent) AND " +
            "(:minScore IS NULL OR a.reviewScore >= :minScore) AND " +
            "(:maxScore IS NULL OR a.reviewScore <= :maxScore)")
    Page<Application> findWithFilters(@Param("eventId") UUID eventId,
                                      @Param("status") ApplicationStatus status,
                                      @Param("email") String email,
                                      @Param("hasGuardianConsent") Boolean hasGuardianConsent,
                                      @Param("minScore") Integer minScore,
                                      @Param("maxScore") Integer maxScore,
                                      Pageable pageable);

    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.event.id = :eventId " +
            "GROUP BY a.status")
    List<Object[]> countApplicationsByStatusForEvent(@Param("eventId") UUID eventId);

    @Query("SELECT AVG(a.reviewScore) FROM Application a " +
            "WHERE a.event.id = :eventId AND a.reviewScore IS NOT NULL")
    Double getAverageReviewScoreForEvent(@Param("eventId") UUID eventId);

    @Query("SELECT a.source, COUNT(a) FROM Application a " +
            "WHERE a.event.id = :eventId AND a.source IS NOT NULL " +
            "GROUP BY a.source ORDER BY COUNT(a) DESC")
    List<Object[]> getApplicationSourcesForEvent(@Param("eventId") UUID eventId);

    @Query("SELECT " +
            "CASE " +
            "  WHEN YEAR(CURRENT_DATE) - YEAR(a.applicantInfo.dateOfBirth) < 10 THEN 'Under 10' " +
            "  WHEN YEAR(CURRENT_DATE) - YEAR(a.applicantInfo.dateOfBirth) < 13 THEN '10-12' " +
            "  WHEN YEAR(CURRENT_DATE) - YEAR(a.applicantInfo.dateOfBirth) < 16 THEN '13-15' " +
            "  WHEN YEAR(CURRENT_DATE) - YEAR(a.applicantInfo.dateOfBirth) < 19 THEN '16-18' " +
            "  ELSE 'Over 18' " +
            "END as ageGroup, COUNT(a) " +
            "FROM Application a WHERE a.event.id = :eventId " +
            "GROUP BY ageGroup")
    List<Object[]> getAgeDistributionForEvent(@Param("eventId") UUID eventId);

    @Modifying
    @Query("UPDATE Application a SET a.status = 'CANCELLED', " +
            "a.cancelledAt = :now, a.cancellationReason = :reason " +
            "WHERE a.event.id = :eventId AND a.status IN ('SUBMITTED', 'ACCEPTED', 'WAITLISTED')")
    int cancelApplicationsForEvent(@Param("eventId") UUID eventId,
                                   @Param("reason") String reason,
                                   @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Application a SET a.waitlistPosition = a.waitlistPosition - 1 " +
            "WHERE a.event.id = :eventId AND a.status = 'WAITLISTED' " +
            "AND a.waitlistPosition > :position")
    int updateWaitlistPositions(@Param("eventId") UUID eventId,
                                @Param("position") Integer position);
}
