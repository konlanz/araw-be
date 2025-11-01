package com.araw.araw.domain.event.entity;

import com.araw.araw.domain.participant.enitity.Participant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_participant_highlights",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "participant_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventParticipantHighlight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Column(name = "headline")
    private String headline;

    @Column(name = "story", columnDefinition = "TEXT")
    private String story;

    @Column(name = "is_featured")
    private Boolean isFeatured = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "media_asset_id")
    private UUID mediaAssetId;

    @Column(name = "photo_url")
    private String photoUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
