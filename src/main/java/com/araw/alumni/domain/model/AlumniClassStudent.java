package com.araw.alumni.domain.model;

import com.araw.shared.exception.DomainValidationException;
import com.araw.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "alumni_class_students")
public class AlumniClassStudent extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumni_class_id", nullable = false)
    private AlumniClass alumniClass;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(name = "role", length = 120)
    private String role;

    @Column(name = "quote", length = 512)
    private String quote;

    @Column(name = "profile", length = 1024)
    private String profile;

    @Column(name = "capstone_project_description", length = 1024)
    private String capstoneProjectDescription;

    @Column(name = "school_attended", length = 160)
    private String schoolAttended;

    @Column(name = "current_location", length = 160)
    private String currentLocation;

    @Column(name = "project_worked_on", length = 255)
    private String projectWorkedOn;

    @Column(name = "photo_media_id")
    private UUID photoMediaId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public static AlumniClassStudent create(String fullName,
                                            String role,
                                            String quote,
                                            String profile,
                                            String capstoneProjectDescription,
                                            String schoolAttended,
                                            String currentLocation,
                                            String projectWorkedOn,
                                            UUID photoMediaId,
                                            Integer displayOrder) {
        AlumniClassStudent student = new AlumniClassStudent();
        student.updateDetails(fullName, role, quote, profile, capstoneProjectDescription, schoolAttended, currentLocation, projectWorkedOn, photoMediaId);
        student.setDisplayOrder(displayOrder);
        return student;
    }

    void assignToClass(AlumniClass alumniClass) {
        this.alumniClass = alumniClass;
    }

    void detachFromClass() {
        this.alumniClass = null;
    }

    public void updateDetails(String fullName,
                              String role,
                              String quote,
                              String profile,
                              String capstoneProjectDescription,
                              String schoolAttended,
                              String currentLocation,
                              String projectWorkedOn,
                              UUID photoMediaId) {
        setFullName(fullName);
        setRole(role);
        setQuote(quote);
        setProfile(profile);
        setCapstoneProjectDescription(capstoneProjectDescription);
        setSchoolAttended(schoolAttended);
        setCurrentLocation(currentLocation);
        setProjectWorkedOn(projectWorkedOn);
        this.photoMediaId = photoMediaId;
    }

    public void setDisplayOrder(Integer displayOrder) {
        if (displayOrder == null || displayOrder < 0) {
            throw new DomainValidationException("Display order must be a non-negative integer");
        }
        this.displayOrder = displayOrder;
    }

    private void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new DomainValidationException("Student full name must not be blank");
        }
        this.fullName = fullName.trim();
    }

    private void setRole(String role) {
        this.role = role != null ? role.trim() : null;
    }

    private void setQuote(String quote) {
        this.quote = quote != null ? quote.trim() : null;
    }

    private void setProfile(String profile) {
        this.profile = profile != null ? profile.trim() : null;
    }

    private void setCapstoneProjectDescription(String capstoneProjectDescription) {
        this.capstoneProjectDescription = capstoneProjectDescription != null ? capstoneProjectDescription.trim() : null;
    }

    private void setSchoolAttended(String schoolAttended) {
        this.schoolAttended = schoolAttended != null ? schoolAttended.trim() : null;
    }

    private void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation != null ? currentLocation.trim() : null;
    }

    private void setProjectWorkedOn(String projectWorkedOn) {
        this.projectWorkedOn = projectWorkedOn != null ? projectWorkedOn.trim() : null;
    }
}
