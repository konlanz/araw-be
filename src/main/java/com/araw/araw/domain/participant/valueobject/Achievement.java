package com.araw.araw.domain.participant.valueobject;

import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.participant.enitity.Participant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "achievement_code", unique = true)
    private String achievementCode;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_type", nullable = false)
    private AchievementType achievementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_level")
    private AchievementLevel achievementLevel;

    @Column(name = "category")
    private String category; // STEM field: "Robotics", "Programming", "Mathematics", etc.

    @Column(name = "earned_date", nullable = false)
    private LocalDateTime earnedDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "badge_image_url")
    private String badgeImageUrl;

    @Column(name = "certificate_url")
    private String certificateUrl;

    @Column(name = "certificate_number")
    private String certificateNumber;

    @ElementCollection
    @CollectionTable(name = "achievement_skills",
            joinColumns = @JoinColumn(name = "achievement_id"))
    @Column(name = "skill")
    private Set<String> skillsDemonstrated = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "achievement_criteria",
            joinColumns = @JoinColumn(name = "achievement_id"))
    @Column(name = "criterion")
    private Set<String> criteriaMetList = new HashSet<>();

    @Column(name = "score")
    private Integer score; // If achievement has a score component

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "percentile")
    private Integer percentile; // Percentile among peers

    @Column(name = "instructor_name")
    private String instructorName;

    @Column(name = "instructor_comments", columnDefinition = "TEXT")
    private String instructorComments;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verification_url")
    private String verificationUrl; // External verification link

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "display_order")
    private Integer displayOrder;

    @ElementCollection
    @CollectionTable(name = "achievement_metadata",
            joinColumns = @JoinColumn(name = "achievement_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // ==================== Domain Methods ====================

    /**
     * Generate achievement code
     */
    @PrePersist
    protected void onCreate() {
        if (this.achievementCode == null) {
            this.achievementCode = generateAchievementCode();
        }
        if (this.earnedDate == null) {
            this.earnedDate = LocalDateTime.now();
        }
    }

    private String generateAchievementCode() {
        String typePrefix = achievementType != null ?
                achievementType.name().substring(0, 3) : "ACH";
        return typePrefix + "-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    /**
     * Verify achievement
     */
    public void verify(String verifierName) {
        if (this.isVerified) {
            throw new IllegalStateException("Achievement is already verified");
        }
        this.isVerified = true;
        this.verifiedBy = verifierName;
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * Feature achievement for display
     */
    public void feature(Integer order) {
        this.isFeatured = true;
        this.displayOrder = order;
    }

    /**
     * Unfeature achievement
     */
    public void unfeature() {
        this.isFeatured = false;
        this.displayOrder = null;
    }

    /**
     * Check if achievement is still valid
     */
    public boolean isValid() {
        if (expiryDate == null) {
            return true;
        }
        return expiryDate.isAfter(LocalDateTime.now());
    }

    /**
     * Check if achievement is expired
     */
    public boolean isExpired() {
        return !isValid();
    }

    /**
     * Calculate achievement percentage
     */
    public Double getPercentage() {
        if (score != null && maxScore != null && maxScore > 0) {
            return (double) score / maxScore * 100;
        }
        return null;
    }

    /**
     * Add skill demonstrated
     */
    public void addSkillDemonstrated(String skill) {
        this.skillsDemonstrated.add(skill);
    }

    /**
     * Add criterion met
     */
    public void addCriterionMet(String criterion) {
        this.criteriaMetList.add(criterion);
    }

    /**
     * Update score
     */
    public void updateScore(Integer newScore) {
        if (maxScore != null && newScore > maxScore) {
            throw new IllegalArgumentException("Score cannot exceed maximum score");
        }
        this.score = newScore;
    }

    /**
     * Generate certificate number if not exists
     */
    public String generateCertificateNumber() {
        if (this.certificateNumber == null) {
            this.certificateNumber = "CERT-" +
                    LocalDateTime.now().getYear() + "-" +
                    achievementType.name().substring(0, 2) + "-" +
                    UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        return this.certificateNumber;
    }

    /**
     * Make achievement private
     */
    public void makePrivate() {
        this.isPublic = false;
        this.isFeatured = false;
    }

    /**
     * Make achievement public
     */
    public void makePublic() {
        this.isPublic = true;
    }
}

