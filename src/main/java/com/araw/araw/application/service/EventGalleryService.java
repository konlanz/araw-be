package com.araw.araw.application.service;

import com.araw.araw.application.dto.event.EventGalleryDto;
import com.araw.araw.application.dto.event.GalleryImageDto;
import com.araw.araw.application.dto.event.GalleryVideoDto;
import com.araw.araw.application.mapper.EventMapper;
import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.event.entity.EventGallery;
import com.araw.araw.domain.event.entity.GalleryImage;
import com.araw.araw.domain.event.entity.GalleryVideo;
import com.araw.araw.domain.event.repository.EventRepository;
import com.araw.araw.domain.event.repository.GalleryImageRepository;
import com.araw.araw.domain.event.repository.GalleryVideoRepository;
import com.araw.araw.domain.event.valueobject.EventStatus;
import com.araw.media.application.MediaStorageService;
import com.araw.media.application.command.MediaUploadCommand;
import com.araw.media.domain.model.MediaAsset;
import com.araw.media.domain.model.MediaCategory;
import com.araw.shared.exception.DomainNotFoundException;
import com.araw.shared.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventGalleryService {

    private static final long MAX_IMAGE_SIZE_BYTES = 25L * 1024 * 1024; // 25 MB
    private static final long MAX_VIDEO_SIZE_BYTES = 500L * 1024 * 1024; // 500 MB

    private final EventRepository eventRepository;
    private final GalleryImageRepository galleryImageRepository;
    private final GalleryVideoRepository galleryVideoRepository;
    private final MediaStorageService mediaStorageService;
    private final EventMapper eventMapper;

    public EventGalleryDto getGallery(UUID eventId) {
        Event event = getEvent(eventId);
        EventGallery gallery = ensureGallery(event);
        EventGalleryDto dto = eventMapper.toEventGalleryDto(gallery);
        populateMediaUrls(dto);
        return dto;
    }

    public GalleryImageDto addImage(UUID eventId,
                                    MultipartFile file,
                                    String caption,
                                    String altText,
                                    String photographer,
                                    Boolean featured) {
        if (file == null || file.isEmpty()) {
            throw new DomainValidationException("Image file is required");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new DomainValidationException("Image file exceeds the maximum size of 25 MB");
        }

        Event event = requireCompletableEvent(eventId);
        EventGallery gallery = ensureGallery(event);

        MediaAsset asset = storeMedia(file, MediaCategory.EVENT_GALLERY_IMAGE,
                "Gallery image for event " + event.getTitle());

        GalleryImage image = GalleryImage.builder()
                .gallery(gallery)
                .imageUrl(asset.getObjectKey())
                .thumbnailUrl(null)
                .caption(caption)
                .altText(altText)
                .photographer(photographer)
                .displayOrder(gallery.getImages().size())
                .isFeatured(Boolean.TRUE.equals(featured))
                .fileSize(asset.getFileSize())
                .mimeType(asset.getContentType())
                .mediaAssetId(asset.getId())
                .originalFileName(asset.getFileName())
                .build();

        gallery.addImage(image);
        GalleryImage saved = galleryImageRepository.save(image);
        GalleryImageDto dto = eventMapper.toGalleryImageDto(saved);
        populateMediaUrl(dto);
        return dto;
    }

    public void removeImage(UUID eventId, UUID imageId) {
        Event event = getEvent(eventId);
        GalleryImage image = galleryImageRepository.findById(imageId)
                .orElseThrow(() -> new DomainNotFoundException("Gallery image not found: " + imageId));

        ensureImageBelongsToEvent(event, image);

        if (image.getMediaAssetId() != null) {
            try {
                mediaStorageService.deleteAsset(image.getMediaAssetId());
            } catch (Exception ex) {
                log.warn("Failed to delete media asset {} for gallery image {}", image.getMediaAssetId(), imageId, ex);
            }
        }

        EventGallery gallery = ensureGallery(event);
        gallery.removeImage(image);
        galleryImageRepository.delete(image);
    }

    public void reorderImages(UUID eventId, List<UUID> imageOrder) {
        Event event = getEvent(eventId);
        EventGallery gallery = ensureGallery(event);
        gallery.reorderImages(imageOrder);
        galleryImageRepository.saveAll(gallery.getImages());
    }

    public GalleryVideoDto addVideo(UUID eventId,
                                    MultipartFile videoFile,
                                    MultipartFile thumbnailFile,
                                    String title,
                                    String description,
                                    Integer durationSeconds) {
        if ((videoFile == null || videoFile.isEmpty())) {
            throw new DomainValidationException("Video file is required");
        }
        if (videoFile.getSize() > MAX_VIDEO_SIZE_BYTES) {
            throw new DomainValidationException("Video file exceeds the maximum size of 500 MB");
        }
        if (thumbnailFile != null && thumbnailFile.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new DomainValidationException("Thumbnail file exceeds the maximum size of 25 MB");
        }

        Event event = requireCompletableEvent(eventId);
        EventGallery gallery = ensureGallery(event);

        MediaAsset videoAsset = storeMedia(videoFile, MediaCategory.EVENT_GALLERY_VIDEO,
                "Gallery video for event " + event.getTitle());

        UUID thumbnailAssetId = null;
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            MediaAsset thumbAsset = storeMedia(thumbnailFile, MediaCategory.EVENT_GALLERY_IMAGE,
                    "Gallery video thumbnail for event " + event.getTitle());
            thumbnailAssetId = thumbAsset.getId();
        }

        GalleryVideo video = GalleryVideo.builder()
                .gallery(gallery)
                .title(title)
                .description(description)
                .mediaAssetId(videoAsset.getId())
                .thumbnailAssetId(thumbnailAssetId)
                .durationSeconds(durationSeconds)
                .displayOrder(gallery.getVideos().size())
                .isPublished(true)
                .build();

        gallery.addVideo(video);
        GalleryVideo saved = galleryVideoRepository.save(video);
        GalleryVideoDto dto = eventMapper.toGalleryVideoDto(saved);
        populateMediaUrl(dto);
        return dto;
    }

    public GalleryVideoDto addVideoLink(UUID eventId,
                                        String externalUrl,
                                        String title,
                                        String description,
                                        Integer durationSeconds) {
        if (externalUrl == null || externalUrl.isBlank()) {
            throw new DomainValidationException("External video URL is required");
        }
        Event event = requireCompletableEvent(eventId);
        EventGallery gallery = ensureGallery(event);

        GalleryVideo video = GalleryVideo.builder()
                .gallery(gallery)
                .externalUrl(externalUrl.trim())
                .title(title)
                .description(description)
                .durationSeconds(durationSeconds)
                .displayOrder(gallery.getVideos().size())
                .isPublished(true)
                .build();

        gallery.addVideo(video);
        GalleryVideo saved = galleryVideoRepository.save(video);
        GalleryVideoDto dto = eventMapper.toGalleryVideoDto(saved);
        populateMediaUrl(dto);
        return dto;
    }

    public void removeVideo(UUID eventId, UUID videoId) {
        Event event = getEvent(eventId);
        GalleryVideo video = galleryVideoRepository.findById(videoId)
                .orElseThrow(() -> new DomainNotFoundException("Gallery video not found: " + videoId));

        ensureVideoBelongsToEvent(event, video);

        if (video.getMediaAssetId() != null) {
            try {
                mediaStorageService.deleteAsset(video.getMediaAssetId());
            } catch (Exception ex) {
                log.warn("Failed to delete media asset {} for gallery video {}", video.getMediaAssetId(), videoId, ex);
            }
        }
        if (video.getThumbnailAssetId() != null) {
            try {
                mediaStorageService.deleteAsset(video.getThumbnailAssetId());
            } catch (Exception ex) {
                log.warn("Failed to delete thumbnail asset {} for gallery video {}", video.getThumbnailAssetId(), videoId, ex);
            }
        }

        EventGallery gallery = ensureGallery(event);
        gallery.removeVideo(video);
        galleryVideoRepository.delete(video);
    }

    public void reorderVideos(UUID eventId, List<UUID> videoOrder) {
        Event event = getEvent(eventId);
        EventGallery gallery = ensureGallery(event);
        gallery.reorderVideos(videoOrder);
        galleryVideoRepository.saveAll(gallery.getVideos());
    }

    public EventGalleryDto updateVisibility(UUID eventId, boolean isPublic) {
        Event event = getEvent(eventId);
        EventGallery gallery = ensureGallery(event);
        gallery.setIsPublic(isPublic);
        eventRepository.save(event);
        EventGalleryDto dto = eventMapper.toEventGalleryDto(gallery);
        populateMediaUrls(dto);
        return dto;
    }

    public void populateMediaUrls(EventGalleryDto gallery) {
        if (gallery == null) {
            return;
        }
        if (gallery.getImages() != null) {
            gallery.getImages().forEach(this::populateMediaUrl);
        }
        if (gallery.getVideos() != null) {
            gallery.getVideos().forEach(this::populateMediaUrl);
        }
    }

    private Event requireCompletableEvent(UUID eventId) {
        Event event = getEvent(eventId);
        if (event.getStatus() != EventStatus.COMPLETED) {
            throw new DomainValidationException("Gallery uploads are only allowed for completed events.");
        }
        return event;
    }

    private Event getEvent(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new DomainNotFoundException("Event not found: " + eventId));
    }

    private EventGallery ensureGallery(Event event) {
        EventGallery gallery = event.getGallery();
        if (gallery == null) {
            gallery = EventGallery.builder()
                    .event(event)
                    .isPublic(false)
                    .title(event.getTitle() + " Gallery")
                    .build();
            event.setGallery(gallery);
            eventRepository.save(event);
        }
        return gallery;
    }

    private MediaAsset storeMedia(MultipartFile file, MediaCategory category, String description) {
        try (MediaUploadCommand command = new MediaUploadCommand(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream(),
                category,
                description
        )) {
            return mediaStorageService.storeMedia(command);
        } catch (IOException ex) {
            throw new DomainValidationException("Failed to read uploaded file", ex);
        }
    }

    private void populateMediaUrl(GalleryImageDto dto) {
        if (dto == null) {
            return;
        }
        if (dto.getMediaAssetId() != null) {
            try {
                dto.setDownloadUrl(mediaStorageService.generatePresignedUrl(dto.getMediaAssetId()));
            } catch (Exception ex) {
                log.warn("Failed to generate presigned URL for gallery image {}", dto.getMediaAssetId(), ex);
            }
        } else if (dto.getImageUrl() != null) {
            dto.setDownloadUrl(dto.getImageUrl());
        }
    }

    private void populateMediaUrl(GalleryVideoDto dto) {
        if (dto == null) {
            return;
        }
        if (dto.getMediaAssetId() != null) {
            try {
                dto.setDownloadUrl(mediaStorageService.generatePresignedUrl(dto.getMediaAssetId()));
            } catch (Exception ex) {
                log.warn("Failed to generate presigned URL for gallery video {}", dto.getMediaAssetId(), ex);
            }
        } else if (dto.getExternalUrl() != null) {
            dto.setDownloadUrl(dto.getExternalUrl());
        }
        if (dto.getThumbnailAssetId() != null) {
            try {
                dto.setThumbnailUrl(mediaStorageService.generatePresignedUrl(dto.getThumbnailAssetId()));
            } catch (Exception ex) {
                log.warn("Failed to generate presigned URL for gallery video thumbnail {}", dto.getThumbnailAssetId(), ex);
            }
        }
    }

    private void ensureImageBelongsToEvent(Event event, GalleryImage image) {
        if (image.getGallery() == null || image.getGallery().getEvent() == null ||
                !image.getGallery().getEvent().getId().equals(event.getId())) {
            throw new DomainValidationException("Gallery image does not belong to the specified event");
        }
    }

    private void ensureVideoBelongsToEvent(Event event, GalleryVideo video) {
        if (video.getGallery() == null || video.getGallery().getEvent() == null ||
                !video.getGallery().getEvent().getId().equals(event.getId())) {
            throw new DomainValidationException("Gallery video does not belong to the specified event");
        }
    }
}
