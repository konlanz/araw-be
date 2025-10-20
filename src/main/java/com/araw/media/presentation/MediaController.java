package com.araw.media.presentation;

import com.araw.media.application.MediaStorageService;
import com.araw.media.application.command.MediaUploadCommand;
import com.araw.media.domain.model.MediaAsset;
import com.araw.media.domain.model.MediaCategory;
import com.araw.media.presentation.dto.MediaAssetResponse;
import com.araw.media.presentation.mapper.MediaAssetMapper;
import com.araw.shared.api.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaStorageService mediaStorageService;
    private final MediaAssetMapper mapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaAssetResponse uploadMedia(@RequestPart("file") MultipartFile file,
                                          @RequestParam("category") MediaCategory category,
                                          @RequestParam(value = "description", required = false) String description) throws IOException {
        try (var command = new MediaUploadCommand(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream(),
                category,
                description
        )) {
            MediaAsset asset = mediaStorageService.storeMedia(command);
            String url = mediaStorageService.generatePresignedUrl(asset.getId());
            return mapper.toResponse(asset, url);
        }
    }

    @GetMapping("/{assetId}")
    public MediaAssetResponse getAsset(@PathVariable UUID assetId) {
        MediaAsset asset = mediaStorageService.getAsset(assetId);
        String url = mediaStorageService.generatePresignedUrl(assetId);
        return mapper.toResponse(asset, url);
    }

    @GetMapping("/{assetId}/url")
    public Map<String, String> getPresignedUrl(@PathVariable UUID assetId) {
        String url = mediaStorageService.generatePresignedUrl(assetId);
        return Map.of("url", url);
    }

    @GetMapping
    public PagedResponse<MediaAssetResponse> listByCategory(@RequestParam("category") MediaCategory category,
                                                            Pageable pageable) {
        Page<MediaAssetResponse> page = mediaStorageService.listByCategory(category, pageable)
                .map(asset -> mapper.toResponse(asset, mediaStorageService.generatePresignedUrl(asset.getId())));
        return PagedResponse.fromPage(page);
    }

    @DeleteMapping("/{assetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAsset(@PathVariable UUID assetId) {
        mediaStorageService.deleteAsset(assetId);
    }
}
