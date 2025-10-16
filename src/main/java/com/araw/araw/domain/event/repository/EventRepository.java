package com.araw.araw.domain.event.repository;

import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.event.valueobject.EventStatus;
import com.araw.araw.domain.event.valueobject.EventType;
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
public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByStatus(EventStatus status);

    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    List<Event> findByStatusIn(List<EventStatus> statuses);

    Page<Event> findByIsPublishedTrue(Pageable pageable);

    Page<Event> findByIsPublishedTrueAndStatus(EventStatus status, Pageable pageable);

    List<Event> findByIsFeaturedTrueAndIsPublishedTrue();

    List<Event> findByIsFeaturedTrueAndStatusOrderByCreatedAtDesc(EventStatus status);

    List<Event> findByEventType(EventType eventType);

    Page<Event> findByEventTypeAndStatus(EventType eventType, EventStatus status, Pageable pageable);

    @Query("SELECT e FROM Event e JOIN e.eventDates ed WHERE ed.sessionDate >= :date " +
            "AND e.status = :status ORDER BY ed.sessionDate ASC")
    List<Event> findUpcomingEvents(@Param("date") LocalDateTime date,
                                   @Param("status") EventStatus status);

    @Query("SELECT e FROM Event e JOIN e.eventDates ed WHERE ed.sessionDate BETWEEN :startDate AND :endDate")
    List<Event> findEventsInDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM Event e WHERE e.applicationDeadline > :now AND e.status = 'UPCOMING'")
    List<Event> findEventsWithOpenApplications(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM Event e WHERE e.registrationClosesAt > :now " +
            "AND e.registrationOpensAt <= :now AND e.status = 'UPCOMING'")
    List<Event> findEventsWithOpenRegistration(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM Event e WHERE e.maxParticipants IS NOT NULL " +
            "AND e.participantCount < e.maxParticipants AND e.status = :status")
    List<Event> findEventsWithAvailableCapacity(@Param("status") EventStatus status);

    @Query("SELECT e FROM Event e WHERE e.maxParticipants IS NOT NULL " +
            "AND e.participantCount >= e.maxParticipants")
    List<Event> findFullEvents();

    @Query("SELECT e FROM Event e WHERE " +
            "(:age BETWEEN e.minAge AND e.maxAge OR " +
            "(e.minAge IS NULL AND :age <= e.maxAge) OR " +
            "(e.maxAge IS NULL AND :age >= e.minAge) OR " +
            "(e.minAge IS NULL AND e.maxAge IS NULL))")
    List<Event> findEventsForAge(@Param("age") Integer age);

    @Query("SELECT DISTINCT e FROM Event e JOIN e.targetGrades tg WHERE tg IN :grades")
    List<Event> findByTargetGradesIn(@Param("grades") List<String> grades);

    @Query("SELECT e FROM Event e WHERE e.location.city = :city")
    List<Event> findByCity(@Param("city") String city);

    @Query("SELECT e FROM Event e WHERE e.location.isVirtual = true")
    List<Event> findVirtualEvents();

    @Query("SELECT e FROM Event e WHERE e.location.isHybrid = true")
    List<Event> findHybridEvents();

    @Query("SELECT e FROM Event e WHERE e.location.stateProvince = :state")
    List<Event> findByState(@Param("state") String state);

    List<Event> findByIsFreeTrue();

    @Query("SELECT e FROM Event e WHERE e.isFree = false AND e.cost <= :maxCost")
    List<Event> findPaidEventsUnderCost(@Param("maxCost") Double maxCost);

    @Query("SELECT e FROM Event e WHERE " +
            "LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Event> searchEvents(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT DISTINCT e FROM Event e " +
            "LEFT JOIN e.eventDates ed " +
            "WHERE (:status IS NULL OR e.status = :status) " +
            "AND (:eventType IS NULL OR e.eventType = :eventType) " +
            "AND (:isVirtual IS NULL OR e.location.isVirtual = :isVirtual) " +
            "AND (:isFree IS NULL OR e.isFree = :isFree) " +
            "AND (:city IS NULL OR e.location.city = :city) " +
            "AND (:minDate IS NULL OR ed.sessionDate >= :minDate) " +
            "AND (:maxDate IS NULL OR ed.sessionDate <= :maxDate)")
    Page<Event> findEventsWithFilters(@Param("status") EventStatus status,
                                      @Param("eventType") EventType eventType,
                                      @Param("isVirtual") Boolean isVirtual,
                                      @Param("isFree") Boolean isFree,
                                      @Param("city") String city,
                                      @Param("minDate") LocalDateTime minDate,
                                      @Param("maxDate") LocalDateTime maxDate,
                                      Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.createdBy.id = :adminId")
    List<Event> findByCreatedByAdminId(@Param("adminId") UUID adminId);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = :status")
    long countByStatus(@Param("status") EventStatus status);

    @Query("SELECT e.eventType, COUNT(e) FROM Event e GROUP BY e.eventType")
    List<Object[]> countEventsByType();

    @Query("SELECT SUM(e.participantCount) FROM Event e WHERE e.status = 'COMPLETED'")
    Long getTotalParticipantsServed();

    @Query("SELECT AVG(e.participantCount) FROM Event e WHERE e.status = 'COMPLETED'")
    Double getAverageParticipantsPerEvent();

    @Modifying
    @Query("UPDATE Event e SET e.viewCount = e.viewCount + 1 WHERE e.id = :eventId")
    void incrementViewCount(@Param("eventId") UUID eventId);

    @Modifying
    @Query("UPDATE Event e SET e.applicationCount = e.applicationCount + 1 WHERE e.id = :eventId")
    void incrementApplicationCount(@Param("eventId") UUID eventId);

    @Modifying
    @Query("UPDATE Event e SET e.applicationCount = e.applicationCount - 1 " +
            "WHERE e.id = :eventId AND e.applicationCount > 0")
    void decrementApplicationCount(@Param("eventId") UUID eventId);

    @Modifying
    @Query("UPDATE Event e SET e.participantCount = e.participantCount + 1 WHERE e.id = :eventId")
    void incrementParticipantCount(@Param("eventId") UUID eventId);

    @Modifying
    @Query("UPDATE Event e SET e.status = 'IN_PROGRESS' " +
            "WHERE e.status = 'UPCOMING' AND e.id IN " +
            "(SELECT DISTINCT ev.id FROM Event ev JOIN ev.eventDates ed " +
            "WHERE ed.sessionDate <= :now)")
    int startEventsWithPastStartDate(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Event e SET e.status = 'COMPLETED' " +
            "WHERE e.status = 'IN_PROGRESS' AND e.id IN " +
            "(SELECT DISTINCT ev.id FROM Event ev JOIN ev.eventDates ed " +
            "GROUP BY ev.id HAVING MAX(ed.sessionEndDate) < :now)")
    int completeEventsWithPastEndDate(@Param("now") LocalDateTime now);
}