package com.araw.araw.application.dto.participant;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantProgressDto {
    private UUID id;
    private UUID eventId;
    private String eventTitle;
    private String progressType;
    private String title;
    private String description;
    private LocalDateTime achievementDate;
    private String recordedBy;
}