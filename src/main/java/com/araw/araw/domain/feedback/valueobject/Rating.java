package com.araw.araw.domain.feedback.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Column(name = "overall_rating")
    private Integer overallRating;

    @Column(name = "content_rating")
    private Integer contentRating;

    @Column(name = "instructor_rating")
    private Integer instructorRating;

    @Column(name = "organization_rating")
    private Integer organizationRating;

    @Column(name = "venue_rating")
    private Integer venueRating;

    @Column(name = "value_rating")
    private Integer valueRating;

    public Double getAverageRating() {
        int count = 0;
        int sum = 0;

        if (overallRating != null) { sum += overallRating; count++; }
        if (contentRating != null) { sum += contentRating; count++; }
        if (instructorRating != null) { sum += instructorRating; count++; }
        if (organizationRating != null) { sum += organizationRating; count++; }
        if (venueRating != null) { sum += venueRating; count++; }
        if (valueRating != null) { sum += valueRating; count++; }

        return count > 0 ? (double) sum / count : null;
    }
}

