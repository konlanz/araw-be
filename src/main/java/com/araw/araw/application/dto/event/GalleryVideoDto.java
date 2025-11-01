package com.araw.araw.application.dto.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GalleryVideoDto {
    private UUID id;
    private String title;
    private String description;
    private String downloadUrl;
    private UUID mediaAssetId;
    private String externalUrl;
    private Integer durationSeconds;
    private Integer displayOrder;
    private Boolean isPublished;
    private LocalDateTime uploadedAt;
    private String thumbnailUrl;
    private UUID thumbnailAssetId;
}
