package com.araw.araw.domain.participant.repository;

import com.araw.araw.domain.application.valueobject.EducationLevel;
import com.araw.araw.domain.participant.enitity.Participant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

    Optional<Participant> findByParticipantCode(String participantCode);

    List<Participant> findByContactInfoEmail(String email);

    Optional<Participant> findByContactInfoEmailIgnoreCase(String email);

    List<Participant> findByContactInfoPhoneNumber(String phoneNumber);

    boolean existsByParticipantCode(String participantCode);

    boolean existsByContactInfoEmail(String email);

    boolean existsByContactInfoEmailAndIdNot(String email, UUID id);

    List<Participant> findByFirstNameAndLastName(String firstName, String lastName);

    List<Participant> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    @Query("SELECT p FROM Participant p WHERE " +
            "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.preferredName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Participant> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT p FROM Participant p WHERE " +
            "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.contactInfo.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "p.participantCode LIKE CONCAT('%', :searchTerm, '%')")
    Page<Participant> searchParticipants(@Param("searchTerm") String searchTerm, Pageable pageable);

    List<Participant> findByEducationLevel(EducationLevel educationLevel);

    List<Participant> findBySchoolName(String schoolName);

    List<Participant> findByGradeLevel(String gradeLevel);

    @Query("SELECT p FROM Participant p WHERE p.graduationYear = :year")
    List<Participant> findByGraduationYear(@Param("year") Integer year);

    @Query("SELECT p FROM Participant p WHERE p.graduationYear BETWEEN :startYear AND :endYear")
    List<Participant> findByGraduationYearBetween(@Param("startYear") Integer startYear,
                                                  @Param("endYear") Integer endYear);

    List<Participant> findByIsAlumniTrue();

    List<Participant> findByIsAlumniFalse();

    List<Participant> findByIsFeaturedAlumniTrue();

    Page<Participant> findByIsAlumniTrue(Pageable pageable);

    @Query("SELECT p FROM Participant p WHERE p.isAlumni = true AND p.isFeaturedAlumni = true " +
            "ORDER BY p.totalEventsAttended DESC")
    List<Participant> findFeaturedAlumniOrderByEngagement();

    @Query("SELECT p FROM Participant p WHERE p.dateOfBirth = :date")
    List<Participant> findByDateOfBirth(@Param("date") LocalDate date);

    @Query("SELECT p FROM Participant p WHERE p.dateOfBirth BETWEEN :startDate AND :endDate")
    List<Participant> findByDateOfBirthBetween(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Participant p WHERE YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) = :age")
    List<Participant> findByAge(@Param("age") Integer age);

    @Query("SELECT p FROM Participant p WHERE " +
            "YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) BETWEEN :minAge AND :maxAge")
    List<Participant> findByAgeBetween(@Param("minAge") Integer minAge,
                                       @Param("maxAge") Integer maxAge);

    List<Participant> findByGender(String gender);

    @Query("SELECT p FROM Participant p WHERE p.contactInfo.city = :city")
    List<Participant> findByCity(@Param("city") String city);

    @Query("SELECT p FROM Participant p WHERE p.contactInfo.stateProvince = :state")
    List<Participant> findByState(@Param("state") String state);

    @Query("SELECT p FROM Participant p WHERE p.contactInfo.country = :country")
    List<Participant> findByCountry(@Param("country") String country);

    @Query("SELECT p FROM Participant p WHERE p.contactInfo.postalCode = :postalCode")
    List<Participant> findByPostalCode(@Param("postalCode") String postalCode);

    @Query("SELECT DISTINCT p FROM Participant p JOIN p.interests i WHERE i IN :interests")
    List<Participant> findByInterestsIn(@Param("interests") List<String> interests);

    @Query("SELECT DISTINCT p FROM Participant p JOIN p.skills s WHERE s IN :skills")
    List<Participant> findBySkillsIn(@Param("skills") List<String> skills);

    @Query("SELECT DISTINCT p FROM Participant p JOIN p.interests i WHERE LOWER(i) LIKE LOWER(CONCAT('%', :interest, '%'))")
    List<Participant> findByInterestContaining(@Param("interest") String interest);

    @Query("SELECT p FROM Participant p WHERE SIZE(p.attendedEvents) >= :minEvents")
    List<Participant> findByMinimumEventsAttended(@Param("minEvents") Integer minEvents);

    @Query("SELECT p FROM Participant p WHERE p.totalEventsAttended >= :count")
    List<Participant> findByTotalEventsAttendedGreaterThanEqual(@Param("count") Integer count);

    @Query("SELECT p FROM Participant p WHERE p.totalHoursParticipated >= :hours")
    List<Participant> findByTotalHoursParticipatedGreaterThanEqual(@Param("hours") Integer hours);

    @Query("SELECT p FROM Participant p JOIN p.attendedEvents e WHERE e.id = :eventId")
    List<Participant> findByAttendedEventId(@Param("eventId") UUID eventId);

    @Query("SELECT COUNT(p) FROM Participant p JOIN p.attendedEvents e WHERE e.id = :eventId")
    Long countParticipantsByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT p FROM Participant p WHERE SIZE(p.achievements) >= :minAchievements")
    List<Participant> findByMinimumAchievements(@Param("minAchievements") Integer minAchievements);

    @Query("SELECT DISTINCT p FROM Participant p JOIN p.achievements a WHERE a.isVerified = true")
    List<Participant> findParticipantsWithVerifiedAchievements();

    @Query("SELECT DISTINCT p FROM Participant p JOIN p.achievements a WHERE a.isFeatured = true")
    List<Participant> findParticipantsWithFeaturedAchievements();

    List<Participant> findByCurrentInstitution(String institution);

    List<Participant> findByCurrentField(String field);

    @Query("SELECT p FROM Participant p WHERE p.currentStatus IS NOT NULL")
    List<Participant> findParticipantsWithCurrentStatus();

    List<Participant> findByConsentForCommunicationTrue();

    List<Participant> findByConsentForPhotosTrue();

    List<Participant> findByConsentForTestimonialsTrue();

    @Query("SELECT p FROM Participant p WHERE " +
            "p.consentForCommunication = true AND " +
            "p.consentForPhotos = true AND " +
            "p.consentForTestimonials = true")
    List<Participant> findWithFullConsent();

    @Query("SELECT p FROM Participant p WHERE p.joinedAt >= :date")
    List<Participant> findParticipantsJoinedAfter(@Param("date") LocalDateTime date);

    @Query("SELECT p FROM Participant p WHERE p.joinedAt BETWEEN :startDate AND :endDate")
    List<Participant> findParticipantsJoinedBetween(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Participant p WHERE p.updatedAt >= :date")
    List<Participant> findRecentlyActive(@Param("date") LocalDateTime date);

    @Query("SELECT DISTINCT p FROM Participant p " +
            "WHERE (:educationLevel IS NULL OR p.educationLevel = :educationLevel) " +
            "AND (:isAlumni IS NULL OR p.isAlumni = :isAlumni) " +
            "AND (:city IS NULL OR p.contactInfo.city = :city) " +
            "AND (:minAge IS NULL OR YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) >= :minAge) " +
            "AND (:maxAge IS NULL OR YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) <= :maxAge) " +
            "AND (:minEvents IS NULL OR p.totalEventsAttended >= :minEvents)")
    Page<Participant> findWithFilters(@Param("educationLevel") EducationLevel educationLevel,
                                      @Param("isAlumni") Boolean isAlumni,
                                      @Param("city") String city,
                                      @Param("minAge") Integer minAge,
                                      @Param("maxAge") Integer maxAge,
                                      @Param("minEvents") Integer minEvents,
                                      Pageable pageable);

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.isAlumni = true")
    Long countAlumni();

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.joinedAt >= :date")
    Long countNewParticipantsSince(@Param("date") LocalDateTime date);

    @Query("SELECT AVG(p.totalEventsAttended) FROM Participant p")
    Double getAverageEventsAttended();

    @Query("SELECT AVG(p.totalHoursParticipated) FROM Participant p")
    Double getAverageHoursParticipated();

    @Query("SELECT p.educationLevel, COUNT(p) FROM Participant p " +
            "GROUP BY p.educationLevel")
    List<Object[]> countByEducationLevel();

    @Query("SELECT p.gradeLevel, COUNT(p) FROM Participant p " +
            "WHERE p.gradeLevel IS NOT NULL GROUP BY p.gradeLevel")
    List<Object[]> countByGradeLevel();

    @Query("SELECT p.contactInfo.city, COUNT(p) FROM Participant p " +
            "WHERE p.contactInfo.city IS NOT NULL GROUP BY p.contactInfo.city " +
            "ORDER BY COUNT(p) DESC")
    List<Object[]> getTopCitiesByParticipantCount();

    @Query("SELECT p.currentField, COUNT(p) FROM Participant p " +
            "WHERE p.currentField IS NOT NULL AND p.isAlumni = true " +
            "GROUP BY p.currentField ORDER BY COUNT(p) DESC")
    List<Object[]> getAlumniFieldDistribution();

    @Query("SELECT " +
            "CASE " +
            "  WHEN YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) < 10 THEN 'Under 10' " +
            "  WHEN YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) < 13 THEN '10-12' " +
            "  WHEN YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) < 16 THEN '13-15' " +
            "  WHEN YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) < 19 THEN '16-18' " +
            "  ELSE 'Over 18' " +
            "END as ageGroup, COUNT(p) " +
            "FROM Participant p WHERE p.dateOfBirth IS NOT NULL " +
            "GROUP BY ageGroup")
    List<Object[]> getAgeDistribution();

    @Query("SELECT p FROM Participant p ORDER BY p.totalEventsAttended DESC")
    Page<Participant> findTopByEventsAttended(Pageable pageable);

    @Query("SELECT p FROM Participant p ORDER BY p.totalHoursParticipated DESC")
    Page<Participant> findTopByHoursParticipated(Pageable pageable);

    @Query("SELECT p FROM Participant p ORDER BY SIZE(p.achievements) DESC")
    Page<Participant> findTopByAchievementCount(Pageable pageable);

    @Modifying
    @Query("UPDATE Participant p SET p.totalEventsAttended = p.totalEventsAttended + 1 " +
            "WHERE p.id = :participantId")
    void incrementEventsAttended(@Param("participantId") UUID participantId);

    @Modifying
    @Query("UPDATE Participant p SET p.totalHoursParticipated = p.totalHoursParticipated + :hours " +
            "WHERE p.id = :participantId")
    void addParticipationHours(@Param("participantId") UUID participantId,
                               @Param("hours") Integer hours);

    @Modifying
    @Query("UPDATE Participant p SET p.isAlumni = true " +
            "WHERE p.totalEventsAttended >= :eventThreshold " +
            "OR p.totalHoursParticipated >= :hoursThreshold")
    int promoteToAlumni(@Param("eventThreshold") Integer eventThreshold,
                        @Param("hoursThreshold") Integer hoursThreshold);
}
