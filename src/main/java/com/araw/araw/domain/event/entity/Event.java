package com.araw.araw.domain.event.entity;

import com.araw.araw.domain.admin.entity.Admin;
import com.araw.araw.domain.event.valueobject.EventStatus;
import com.araw.araw.domain.event.valueobject.EventType;
import com.araw.araw.domain.event.valueobject.Location;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Embedded
    private Location location;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "application_link", length = 500)
    private String applicationLink;

    @Column(name = "application_slug", unique = true, length = 160)
    private String applicationSlug;

    @Column(name = "application_link_generated_at")
    private LocalDateTime applicationLinkGeneratedAt;

    @Column(name = "application_deadline")
    private LocalDateTime applicationDeadline;

    @Column(name = "registration_opens_at")
    private LocalDateTime registrationOpensAt;

    @Column(name = "registration_closes_at")
    private LocalDateTime registrationClosesAt;

    @Column(name = "is_free")
    private Boolean isFree;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @ElementCollection
    @CollectionTable(name = "event_prerequisites",
            joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "prerequisite")
    private Set<String> prerequisites = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "event_learning_outcomes",
            joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "outcome")
    private Set<String> learningOutcomes = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "event_target_grades",
            joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "grade")
    private Set<String> targetGrades = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sessionDate ASC")
    private List<EventDate> eventDates = new ArrayList<>();

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private EventGallery gallery;

    @Column(name = "banner_image_url")
    private String bannerImageUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id")
    private Admin createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by_admin_id")
    private Admin lastModifiedBy;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "application_count")
    private Integer applicationCount = 0;

    @Column(name = "participant_count")
    private Integer participantCount = 0;

    @Column(name = "feedback_enabled")
    private Boolean feedbackEnabled = false;

    @Column(name = "feedback_opens_at")
    private LocalDateTime feedbackOpensAt;

    @Column(name = "feedback_closes_at")
    private LocalDateTime feedbackClosesAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public void publish() {
        if (this.status != EventStatus.DRAFT) {
            throw new IllegalStateException("Only draft events can be published");
        }
        this.status = EventStatus.UPCOMING;
        this.isPublished = true;
        this.publishedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status == EventStatus.COMPLETED || this.status == EventStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel a completed or already cancelled event");
        }
        this.status = EventStatus.CANCELLED;
        this.cancellationReason = reason != null ? reason.trim() : null;
    }

    public void start() {
        if (this.status != EventStatus.UPCOMING) {
            throw new IllegalStateException("Only upcoming events can be started");
        }
        this.status = EventStatus.IN_PROGRESS;
    }

    public void complete() {
        if (this.status != EventStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress events can be completed");
        }
        this.status = EventStatus.COMPLETED;
    }

    public void addEventDate(EventDate eventDate) {
        eventDates.add(eventDate);
        eventDate.setEvent(this);
    }

    public void removeEventDate(EventDate eventDate) {
        eventDates.remove(eventDate);
        eventDate.setEvent(null);
    }

    public boolean isRegistrationOpen() {
        LocalDateTime now = LocalDateTime.now();
        return registrationOpensAt != null &&
                registrationClosesAt != null &&
                now.isAfter(registrationOpensAt) &&
                now.isBefore(registrationClosesAt) &&
                status == EventStatus.UPCOMING;
    }

    public boolean hasCapacity() {
        return maxParticipants == null || participantCount < maxParticipants;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementApplicationCount() {
        this.applicationCount++;
    }

    public void decrementApplicationCount() {
        if (this.applicationCount > 0) {
            this.applicationCount--;
        }
    }

    public boolean isFeedbackWindowOpen() {
        if (!Boolean.TRUE.equals(feedbackEnabled)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (feedbackOpensAt != null && now.isBefore(feedbackOpensAt)) {
            return false;
        }
        if (feedbackClosesAt != null && now.isAfter(feedbackClosesAt)) {
            return false;
        }
        return true;
    }
}
