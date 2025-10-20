package com.araw.araw.domain.application.entity;

import com.araw.araw.domain.application.valueobject.ApplicantInfo;
import com.araw.araw.domain.application.valueobject.ApplicationStatus;
import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.participant.enitity.Participant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "applications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"event_id", "email"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "application_number", unique = true, nullable = false)
    private String applicationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @Embedded
    private ApplicantInfo applicantInfo;

    @Column(name = "email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(name = "motivation_statement", columnDefinition = "TEXT")
    private String motivationStatement;

    @Column(name = "prior_experience", columnDefinition = "TEXT")
    private String priorExperience;

    @Column(name = "learning_goals", columnDefinition = "TEXT")
    private String learningGoals;

    @ElementCollection
    @CollectionTable(name = "application_answers",
            joinColumns = @JoinColumn(name = "application_id"))
    @MapKeyColumn(name = "question")
    @Column(name = "answer", columnDefinition = "TEXT")
    private Map<String, String> customAnswers = new HashMap<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationDocument> documents = new ArrayList<>();

    @Column(name = "guardian_consent")
    private Boolean guardianConsent;

    @Column(name = "guardian_name")
    private String guardianName;

    @Column(name = "guardian_email")
    private String guardianEmail;

    @Column(name = "guardian_phone")
    private String guardianPhone;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relation")
    private String emergencyContactRelation;

    @ElementCollection
    @CollectionTable(name = "application_dietary_restrictions",
            joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "restriction")
    private Set<String> dietaryRestrictions = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "application_medical_conditions",
            joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "condition")
    private Set<String> medicalConditions = new HashSet<>();

    @Column(name = "special_accommodations", columnDefinition = "TEXT")
    private String specialAccommodations;

    @Column(name = "review_score")
    private Integer reviewScore;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "acceptance_sent_at")
    private LocalDateTime acceptanceSentAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "waitlist_position")
    private Integer waitlistPosition;

    @Column(name = "confirmation_token")
    private String confirmationToken;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "source")
    private String source; // How they heard about the event

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Domain Methods
    @PrePersist
    protected void onCreate() {
        if (this.applicationNumber == null) {
            this.applicationNumber = generateApplicationNumber();
        }
        if (this.status == null) {
            this.status = ApplicationStatus.DRAFT;
        }
    }

    private String generateApplicationNumber() {
        return "APP-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void submit() {
        if (this.status != ApplicationStatus.DRAFT) {
            throw new IllegalStateException("Only draft applications can be submitted");
        }
        validateRequiredFields();
        this.status = ApplicationStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    private void validateRequiredFields() {
        if (applicantInfo == null || email == null || email.isBlank()) {
            throw new IllegalStateException("Required fields are missing");
        }
        // Add more validation as needed
    }

    public void review(Integer score, String notes, String reviewerName) {
        if (this.status != ApplicationStatus.SUBMITTED && this.status != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Application must be submitted or under review");
        }
        this.status = ApplicationStatus.UNDER_REVIEW;
        this.reviewScore = score;
        this.reviewNotes = notes;
        this.reviewedBy = reviewerName;
        this.reviewedAt = LocalDateTime.now();
    }

    public void accept() {
        if (this.status != ApplicationStatus.UNDER_REVIEW
                && this.status != ApplicationStatus.SUBMITTED
                && this.status != ApplicationStatus.WAITLISTED) {
            throw new IllegalStateException("Application must be under review, submitted, or waitlisted");
        }
        this.status = ApplicationStatus.ACCEPTED;
        this.acceptanceSentAt = LocalDateTime.now();
        this.confirmationToken = UUID.randomUUID().toString();
        this.waitlistPosition = null;
    }

    public void reject(String reason) {
        if (this.status == ApplicationStatus.CONFIRMED || this.status == ApplicationStatus.CANCELLED) {
            throw new IllegalStateException("Cannot reject confirmed or cancelled applications");
        }
        this.status = ApplicationStatus.REJECTED;
        this.rejectionReason = reason;
    }

    public void waitlist(Integer position) {
        if (this.status != ApplicationStatus.SUBMITTED && this.status != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Only submitted or under review applications can be waitlisted");
        }
        this.status = ApplicationStatus.WAITLISTED;
        this.waitlistPosition = position;
    }

    public void confirm() {
        if (this.status != ApplicationStatus.ACCEPTED) {
            throw new IllegalStateException("Only accepted applications can be confirmed");
        }
        this.status = ApplicationStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status == ApplicationStatus.CANCELLED) {
            throw new IllegalStateException("Application is already cancelled");
        }
        this.status = ApplicationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public void addDocument(ApplicationDocument document) {
        documents.add(document);
        document.setApplication(this);
    }

    public void removeDocument(ApplicationDocument document) {
        documents.remove(document);
        document.setApplication(null);
    }
}
