package com.araw.araw.application.dto.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventGalleryDto {
    private UUID id;
    private String title;
    private String description;
    private List<GalleryImageDto> images;
    private Set<String> videoUrls;
    private Boolean isPublic;
    private LocalDateTime createdAt;
}
