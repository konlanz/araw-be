package com.araw.araw.application.dto.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventParticipantHighlightDto {
    private UUID id;
    private UUID participantId;
    private String participantName;
    private String headline;
    private String story;
    private Boolean isFeatured;
    private Integer displayOrder;
    private String photoUrl;
    private String downloadUrl;
    private UUID mediaAssetId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
