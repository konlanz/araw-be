package com.araw.araw.domain.event.repository;

import com.araw.araw.domain.event.entity.EventParticipantHighlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventParticipantHighlightRepository extends JpaRepository<EventParticipantHighlight, UUID> {

    List<EventParticipantHighlight> findByEventIdOrderByDisplayOrderAscCreatedAtDesc(UUID eventId);

    Optional<EventParticipantHighlight> findByEventIdAndParticipantId(UUID eventId, UUID participantId);
}
