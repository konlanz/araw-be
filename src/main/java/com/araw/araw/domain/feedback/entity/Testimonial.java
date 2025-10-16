package com.araw.araw.domain.feedback.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "testimonials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Testimonial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id")
    private Feedback feedback;

    @Column(name = "quote", columnDefinition = "TEXT", nullable = false)
    private String quote;

    @Column(name = "context")
    private String context;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "author_title")
    private String authorTitle;

    @Column(name = "author_photo_url")
    private String authorPhotoUrl;

    @Column(name = "highlight_text")
    private String highlightText;

    @Column(name = "video_testimonial_url")
    private String videoTestimonialUrl;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_published")
    private Boolean isPublished = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
