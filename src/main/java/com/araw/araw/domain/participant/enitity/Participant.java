package com.araw.araw.domain.participant.enitity;


import com.araw.araw.domain.application.valueobject.ContactInfo;
import com.araw.araw.domain.application.valueobject.EducationLevel;
import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.participant.valueobject.Achievement;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "participant_code", unique = true)
    private String participantCode;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "preferred_name")
    private String preferredName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Embedded
    private ContactInfo contactInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level")
    private EducationLevel educationLevel;

    @Column(name = "school_name")
    private String schoolName;

    @Column(name = "grade_level")
    private String gradeLevel;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @ElementCollection
    @CollectionTable(name = "participant_interests",
            joinColumns = @JoinColumn(name = "participant_id"))
    @Column(name = "interest")
    private Set<String> interests = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "participant_skills",
            joinColumns = @JoinColumn(name = "participant_id"))
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "participant_events",
            joinColumns = @JoinColumn(name = "participant_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    private Set<Event> attendedEvents = new HashSet<>();

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    private List<ParticipantProgress> progressRecords = new ArrayList<>();

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    private List<Achievement> achievements = new ArrayList<>();

    @Column(name = "total_events_attended")
    private Integer totalEventsAttended = 0;

    @Column(name = "total_hours_participated")
    private Integer totalHoursParticipated = 0;

    @Column(name = "is_alumni")
    private Boolean isAlumni = false;

    @Column(name = "alumni_story", columnDefinition = "TEXT")
    private String alumniStory;

    @Column(name = "current_status")
    private String currentStatus;

    @Column(name = "current_institution")
    private String currentInstitution;

    @Column(name = "current_field")
    private String currentField;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "is_featured_alumni")
    private Boolean isFeaturedAlumni = false;

    @Column(name = "consent_for_communication")
    private Boolean consentForCommunication = true;

    @Column(name = "consent_for_photos")
    private Boolean consentForPhotos = true;

    @Column(name = "consent_for_testimonials")
    private Boolean consentForTestimonials = true;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        if (this.participantCode == null) {
            this.participantCode = generateParticipantCode();
        }
    }

    private String generateParticipantCode() {
        return "PART-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public String getFullName() {
        return (preferredName != null ? preferredName : firstName) + " " + lastName;
    }

    public void attendEvent(Event event) {
        attendedEvents.add(event);
        totalEventsAttended++;
    }

    public void addAchievement(Achievement achievement) {
        achievements.add(achievement);
        achievement.setParticipant(this);
    }

    public void updateProgress(ParticipantProgress progress) {
        progressRecords.add(progress);
        progress.setParticipant(this);
    }

    public void promoteToAlumni() {
        this.isAlumni = true;
    }
}