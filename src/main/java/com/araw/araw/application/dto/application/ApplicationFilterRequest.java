package com.araw.araw.application.dto.application;

import com.araw.araw.domain.application.valueobject.ApplicationStatus;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationFilterRequest {
    private UUID eventId;
    private ApplicationStatus status;
    private String email;
    private Boolean hasGuardianConsent;
    private Integer minScore;
    private Integer maxScore;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime submittedAfter;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime submittedBefore;

    private String searchTerm;

    private int page = 0;
    private int size = 20;
    private String sortBy = "submittedAt";
    private String sortDirection = "DESC";
}
