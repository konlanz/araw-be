package com.araw.araw.domain.event.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "event_galleries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventGallery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "gallery", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, uploadedAt DESC")
    private List<GalleryImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "gallery", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, uploadedAt DESC")
    private List<GalleryVideo> videos = new ArrayList<>();

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void addImage(GalleryImage image) {
        images.add(image);
        image.setGallery(this);
    }

    public void removeImage(GalleryImage image) {
        images.remove(image);
        image.setGallery(null);
    }

    public void reorderImages(List<UUID> imageIds) {
        Map<UUID, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < imageIds.size(); i++) {
            orderMap.put(imageIds.get(i), i);
        }

        images.forEach(img -> {
            Integer newOrder = orderMap.get(img.getId());
            if (newOrder != null) {
                img.setDisplayOrder(newOrder);
            }
        });
    }

    public void addVideo(GalleryVideo video) {
        videos.add(video);
        video.setGallery(this);
    }

    public void removeVideo(GalleryVideo video) {
        videos.remove(video);
        video.setGallery(null);
    }

    public void reorderVideos(List<UUID> videoIds) {
        Map<UUID, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < videoIds.size(); i++) {
            orderMap.put(videoIds.get(i), i);
        }
        videos.forEach(video -> {
            Integer newOrder = orderMap.get(video.getId());
            if (newOrder != null) {
                video.setDisplayOrder(newOrder);
            }
        });
    }
}
