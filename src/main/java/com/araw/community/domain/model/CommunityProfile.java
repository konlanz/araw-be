package com.araw.community.domain.model;

import com.araw.shared.exception.DomainValidationException;
import com.araw.shared.persistence.AuditableEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "community_profiles")
public class CommunityProfile extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 140)
    private String slug;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "email", length = 160)
    private String email;

    @Column(name = "phone", length = 40)
    private String phone;

    @Column(name = "headline", length = 160)
    private String headline;

    @Lob
    @Column(name = "biography")
    private String biography;

    @Column(name = "organization", length = 160)
    private String organization;

    @Column(name = "location", length = 160)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type", nullable = false, length = 30)
    private ProfileType profileType;

    @Column(name = "joined_at")
    private OffsetDateTime joinedAt;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "featured", nullable = false)
    private boolean featured = false;

    @Column(name = "profile_media_id")
    private UUID profileMediaId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "community_profile_skills", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "skill", nullable = false, length = 60)
    private Set<String> skills = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "community_profile_interests", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "interest", nullable = false, length = 60)
    private Set<String> interests = new HashSet<>();

    public static CommunityProfile create(String slug,
                                          String firstName,
                                          String lastName,
                                          ProfileType profileType,
                                          OffsetDateTime joinedAt,
                                          String email,
                                          String phone,
                                          String headline,
                                          String biography,
                                          String organization,
                                          String location,
                                          Set<String> skills,
                                          Set<String> interests,
                                          UUID profileMediaId,
                                          boolean featured) {
        CommunityProfile profile = new CommunityProfile();
        profile.setSlug(slug);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.profileType = Objects.requireNonNull(profileType, "Profile type is required");
        profile.joinedAt = joinedAt != null ? joinedAt : OffsetDateTime.now();
        profile.setEmail(email);
        profile.setPhone(phone);
        profile.setHeadline(headline);
        profile.setBiography(biography);
        profile.setOrganization(organization);
        profile.setLocation(location);
        profile.setSkills(skills);
        profile.setInterests(interests);
        profile.profileMediaId = profileMediaId;
        profile.featured = featured;
        profile.active = true;
        return profile;
    }

    public String getFullName() {
        return "%s %s".formatted(firstName, lastName).trim();
    }

    public void updateProfile(String firstName,
                              String lastName,
                              ProfileType profileType,
                              OffsetDateTime joinedAt,
                              String email,
                              String phone,
                              String headline,
                              String biography,
                              String organization,
                              String location,
                              Set<String> skills,
                              Set<String> interests,
                              UUID profileMediaId,
                              Boolean featured,
                              Boolean active) {
        setFirstName(firstName);
        setLastName(lastName);
        if (profileType != null) {
            this.profileType = profileType;
        }
        if (joinedAt != null) {
            this.joinedAt = joinedAt;
        }
        setEmail(email);
        setPhone(phone);
        setHeadline(headline);
        setBiography(biography);
        setOrganization(organization);
        setLocation(location);
        setSkills(skills);
        setInterests(interests);
        this.profileMediaId = profileMediaId;
        if (featured != null) {
            this.featured = featured;
        }
        if (active != null) {
            this.active = active;
        }
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void markFeatured() {
        this.featured = true;
    }

    public void unmarkFeatured() {
        this.featured = false;
    }

    public void setSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new DomainValidationException("Profile slug must not be blank");
        }
        this.slug = slug.trim().toLowerCase();
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new DomainValidationException("First name must not be blank");
        }
        this.firstName = firstName.trim();
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new DomainValidationException("Last name must not be blank");
        }
        this.lastName = lastName.trim();
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public void setHeadline(String headline) {
        this.headline = headline != null ? headline.trim() : null;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public void setOrganization(String organization) {
        this.organization = organization != null ? organization.trim() : null;
    }

    public void setLocation(String location) {
        this.location = location != null ? location.trim() : null;
    }

    public void setSkills(Set<String> skills) {
        this.skills = normalizeCollection(skills);
    }

    public void setInterests(Set<String> interests) {
        this.interests = normalizeCollection(interests);
    }

    private Set<String> normalizeCollection(Set<String> values) {
        return values == null ? new HashSet<>() : values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));
    }
}
