package com.araw.araw.application.dto.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GalleryImageDto {
    private UUID id;
    private String imageUrl;
    private String thumbnailUrl;
    private String caption;
    private String altText;
    private String photographer;
    private Integer displayOrder;
    private Boolean isFeatured;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
    private UUID mediaAssetId;
    private Long fileSize;
    private String mimeType;
}
