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

    @ElementCollection
    @CollectionTable(name = "event_gallery_videos",
            joinColumns = @JoinColumn(name = "gallery_id"))
    @Column(name = "video_url")
    private Set<String> videoUrls = new HashSet<>();

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
}
