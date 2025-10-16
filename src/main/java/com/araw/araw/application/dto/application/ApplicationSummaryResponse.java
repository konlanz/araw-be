package com.araw.araw.application.dto.application;

import com.araw.araw.domain.application.valueobject.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSummaryResponse {
    private UUID id;
    private String applicationNumber;
    private String applicantName;
    private String email;
    private ApplicationStatus status;
    private String eventTitle;
    private Integer reviewScore;
    private Integer waitlistPosition;
    private LocalDateTime submittedAt;
}