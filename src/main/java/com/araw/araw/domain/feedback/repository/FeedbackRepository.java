package com.araw.araw.domain.feedback.repository;

import com.araw.araw.domain.feedback.entity.Feedback;
import com.araw.araw.domain.feedback.valueobject.FeedbackType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    List<Feedback> findByEventId(UUID eventId);

    Page<Feedback> findByEventId(UUID eventId, Pageable pageable);

    List<Feedback> findByEventIdAndFeedbackType(UUID eventId, FeedbackType feedbackType);

    List<Feedback> findByParticipantId(UUID participantId);

    List<Feedback> findByParticipantIdOrderBySubmittedAtDesc(UUID participantId);

    boolean existsByEventIdAndParticipantId(UUID eventId, UUID participantId);

    boolean existsByEventIdAndSubmittedByEmail(UUID eventId, String email);

    List<Feedback> findByConsentToPublishTrueAndPublishedAtIsNotNull();

    Page<Feedback> findByConsentToPublishTrueAndPublishedAtIsNotNull(Pageable pageable);

    List<Feedback> findByIsFeaturedTrueAndConsentToPublishTrue();

    @Query("SELECT f FROM Feedback f WHERE f.isFeatured = true " +
            "AND f.consentToPublish = true ORDER BY f.submittedAt DESC")
    List<Feedback> findFeaturedTestimonials();

    @Query("SELECT f FROM Feedback f WHERE f.testimonial IS NOT NULL " +
            "AND f.consentToPublish = true")
    List<Feedback> findFeedbackWithTestimonials();

    List<Feedback> findByFeedbackType(FeedbackType feedbackType);

    Page<Feedback> findByFeedbackType(FeedbackType feedbackType, Pageable pageable);

    List<Feedback> findByWouldRecommendTrue();

    List<Feedback> findByEventIdAndWouldRecommendTrue(UUID eventId);

    @Query("SELECT f FROM Feedback f WHERE f.rating.overallRating >= :minRating")
    List<Feedback> findByMinimumRating(@Param("minRating") Integer minRating);

    @Query("SELECT f FROM Feedback f WHERE f.event.id = :eventId " +
            "AND f.rating.overallRating >= :minRating")
    List<Feedback> findByEventAndMinimumRating(@Param("eventId") UUID eventId,
                                               @Param("minRating") Integer minRating);

    @Query("SELECT f FROM Feedback f WHERE f.submittedAt >= :date")
    List<Feedback> findFeedbackSubmittedAfter(@Param("date") LocalDateTime date);

    @Query("SELECT f FROM Feedback f WHERE f.submittedAt BETWEEN :startDate AND :endDate")
    List<Feedback> findFeedbackSubmittedBetween(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    List<Feedback> findByFollowUpCompletedFalse();

    @Query("SELECT f FROM Feedback f WHERE f.followUpCompleted = false " +
            "AND f.submittedAt < :date")
    List<Feedback> findPendingFollowUps(@Param("date") LocalDateTime date);

    List<Feedback> findByIsAnonymousTrue();

    List<Feedback> findByEventIdAndIsAnonymousTrue(UUID eventId);

    @Query("SELECT f FROM Feedback f WHERE " +
            "LOWER(f.overallExperience) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(f.whatLearned) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(f.mostValuableAspect) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Feedback> searchFeedback(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT AVG(f.rating.overallRating) FROM Feedback f " +
            "WHERE f.event.id = :eventId AND f.rating.overallRating IS NOT NULL")
    Double getAverageRatingForEvent(@Param("eventId") UUID eventId);

    @Query("SELECT AVG(f.rating.contentRating) FROM Feedback f " +
            "WHERE f.event.id = :eventId AND f.rating.contentRating IS NOT NULL")
    Double getAverageContentRatingForEvent(@Param("eventId") UUID eventId);

    @Query("SELECT AVG(f.rating.instructorRating) FROM Feedback f " +
            "WHERE f.event.id = :eventId AND f.rating.instructorRating IS NOT NULL")
    Double getAverageInstructorRatingForEvent(@Param("eventId") UUID eventId);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.event.id = :eventId " +
            "AND f.wouldRecommend = true")
    Long countRecommendationsForEvent(@Param("eventId") UUID eventId);

    @Query("SELECT " +
            "COUNT(f) as total, " +
            "COUNT(CASE WHEN f.wouldRecommend = true THEN 1 END) as wouldRecommend, " +
            "AVG(f.rating.overallRating) as avgRating " +
            "FROM Feedback f WHERE f.event.id = :eventId")
    Object getFeedbackSummaryForEvent(@Param("eventId") UUID eventId);

    @Query("SELECT skill, COUNT(f) FROM Feedback f JOIN f.skillsGained skill " +
            "WHERE f.event.id = :eventId GROUP BY skill ORDER BY COUNT(f) DESC")
    List<Object[]> getTopSkillsGainedForEvent(@Param("eventId") UUID eventId);

    @Query("SELECT KEY(ar), AVG(VALUE(ar)) FROM Feedback f JOIN f.aspectRatings ar " +
            "WHERE f.event.id = :eventId GROUP BY KEY(ar)")
    List<Object[]> getAverageAspectRatingsForEvent(@Param("eventId") UUID eventId);

    @Query("SELECT f.feedbackType, COUNT(f) FROM Feedback f " +
            "WHERE f.event.id = :eventId GROUP BY f.feedbackType")
    List<Object[]> getFeedbackTypeDistributionForEvent(@Param("eventId") UUID eventId);

     @Query("SELECT " +
            "COUNT(CASE WHEN f.rating.overallRating >= 9 THEN 1 END) as promoters, " +
            "COUNT(CASE WHEN f.rating.overallRating BETWEEN 7 AND 8 THEN 1 END) as passives, " +
            "COUNT(CASE WHEN f.rating.overallRating <= 6 THEN 1 END) as detractors, " +
            "COUNT(f) as total " +
            "FROM Feedback f WHERE f.event.id = :eventId " +
            "AND f.rating.overallRating IS NOT NULL")
    Object getNPSDataForEvent(@Param("eventId") UUID eventId);
}
