package com.araw.araw.presentation;

import com.araw.araw.application.dto.event.EventGalleryDto;
import com.araw.araw.application.dto.event.GalleryImageDto;
import com.araw.araw.application.dto.event.GalleryVideoDto;
import com.araw.araw.application.dto.event.GalleryVideoLinkRequest;
import com.araw.araw.application.dto.event.UpdateGalleryVisibilityRequest;
import com.araw.araw.application.service.EventGalleryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/araw/events/{eventId}/gallery")
@RequiredArgsConstructor
public class EventGalleryController {

    private final EventGalleryService eventGalleryService;

    @GetMapping
    public EventGalleryDto getGallery(@PathVariable UUID eventId) {
        return eventGalleryService.getGallery(eventId);
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public GalleryImageDto uploadImage(@PathVariable UUID eventId,
                                       @RequestPart("file") MultipartFile file,
                                       @RequestParam(value = "caption", required = false) String caption,
                                       @RequestParam(value = "altText", required = false) String altText,
                                       @RequestParam(value = "photographer", required = false) String photographer,
                                       @RequestParam(value = "featured", required = false) Boolean featured) {
        return eventGalleryService.addImage(eventId, file, caption, altText, photographer, featured);
    }

    @DeleteMapping("/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@PathVariable UUID eventId, @PathVariable UUID imageId) {
        eventGalleryService.removeImage(eventId, imageId);
    }

    @PatchMapping("/images/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderImages(@PathVariable UUID eventId,
                              @RequestBody List<UUID> imageOrder) {
        eventGalleryService.reorderImages(eventId, imageOrder);
    }

    @PostMapping(value = "/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public GalleryVideoDto uploadVideo(@PathVariable UUID eventId,
                                       @RequestPart("file") MultipartFile video,
                                       @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
                                       @RequestParam(value = "title", required = false) String title,
                                       @RequestParam(value = "description", required = false) String description,
                                       @RequestParam(value = "durationSeconds", required = false) Integer durationSeconds) {
        return eventGalleryService.addVideo(eventId, video, thumbnail, title, description, durationSeconds);
    }

    @PostMapping(value = "/videos/link")
    @ResponseStatus(HttpStatus.CREATED)
    public GalleryVideoDto addVideoLink(@PathVariable UUID eventId,
                                        @Valid @RequestBody GalleryVideoLinkRequest request) {
        return eventGalleryService.addVideoLink(eventId,
                request.getExternalUrl(),
                request.getTitle(),
                request.getDescription(),
                request.getDurationSeconds());
    }

    @DeleteMapping("/videos/{videoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVideo(@PathVariable UUID eventId, @PathVariable UUID videoId) {
        eventGalleryService.removeVideo(eventId, videoId);
    }

    @PatchMapping("/videos/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderVideos(@PathVariable UUID eventId,
                              @RequestBody List<UUID> videoOrder) {
        eventGalleryService.reorderVideos(eventId, videoOrder);
    }

    @PatchMapping("/visibility")
    public EventGalleryDto updateVisibility(@PathVariable UUID eventId,
                                            @Valid @RequestBody UpdateGalleryVisibilityRequest request) {
        return eventGalleryService.updateVisibility(eventId, request.getIsPublic());
    }
}
