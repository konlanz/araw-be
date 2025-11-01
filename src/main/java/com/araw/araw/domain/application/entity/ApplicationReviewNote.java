package com.araw.araw.domain.application.entity;

import com.araw.araw.domain.admin.entity.Admin;
import com.araw.araw.domain.application.valueobject.ApplicationReviewCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "application_review_notes")
public class ApplicationReviewNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin reviewer;

    @Column(name = "reviewer_name", length = 160)
    private String reviewerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 40, nullable = false)
    private ApplicationReviewCategory category;

    @Column(name = "score")
    private Integer score;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "notes", columnDefinition = "TEXT", nullable = false)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
