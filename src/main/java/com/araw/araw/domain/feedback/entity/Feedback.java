package com.araw.araw.domain.feedback.entity;

import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.feedback.valueobject.FeedbackType;
import com.araw.araw.domain.feedback.valueobject.Rating;
import com.araw.araw.domain.participant.enitity.Participant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @Column(name = "submitted_by_name")
    private String submittedByName;

    @Column(name = "submitted_by_email")
    private String submittedByEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type")
    private FeedbackType feedbackType;

    @Embedded
    private Rating rating;

    @Column(name = "overall_experience", columnDefinition = "TEXT")
    private String overallExperience;

    @Column(name = "what_learned", columnDefinition = "TEXT")
    private String whatLearned;

    @Column(name = "most_valuable", columnDefinition = "TEXT")
    private String mostValuableAspect;

    @Column(name = "improvement_suggestions", columnDefinition = "TEXT")
    private String improvementSuggestions;

    @Column(name = "would_recommend")
    private Boolean wouldRecommend;

    @Column(name = "recommendation_reason", columnDefinition = "TEXT")
    private String recommendationReason;

    @ElementCollection
    @CollectionTable(name = "feedback_skills_gained",
            joinColumns = @JoinColumn(name = "feedback_id"))
    @Column(name = "skill")
    private Set<String> skillsGained = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "feedback_ratings",
            joinColumns = @JoinColumn(name = "feedback_id"))
    @MapKeyColumn(name = "aspect")
    @Column(name = "rating")
    private Map<String, Integer> aspectRatings = new HashMap<>();

    @OneToOne(mappedBy = "feedback", cascade = CascadeType.ALL)
    private Testimonial testimonial;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    @Column(name = "consent_to_publish")
    private Boolean consentToPublish = false;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "follow_up_completed")
    private Boolean followUpCompleted = false;

    @Column(name = "follow_up_notes", columnDefinition = "TEXT")
    private String followUpNotes;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public void publish() {
        if (!consentToPublish) {
            throw new IllegalStateException("Cannot publish without consent");
        }
        this.publishedAt = LocalDateTime.now();
    }

    public void feature() {
        if (!consentToPublish) {
            throw new IllegalStateException("Cannot feature without consent");
        }
        this.isFeatured = true;
    }

    public void createTestimonial(String quote, String context) {
        if (this.testimonial == null) {
            this.testimonial = Testimonial.builder()
                    .feedback(this)
                    .quote(quote)
                    .context(context)
                    .authorName(isAnonymous ? "Anonymous" : submittedByName)
                    .build();
        }
    }
}

