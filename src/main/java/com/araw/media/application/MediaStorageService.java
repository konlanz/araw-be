package com.araw.media.application;

import com.araw.media.application.command.MediaUploadCommand;
import com.araw.media.config.MinioProperties;
import com.araw.media.domain.exception.MediaStorageException;
import com.araw.media.domain.model.MediaAsset;
import com.araw.media.domain.model.MediaCategory;
import com.araw.media.domain.repository.MediaAssetRepository;
import com.araw.shared.exception.DomainNotFoundException;
import com.araw.shared.text.SlugGenerator;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Transactional
public class MediaStorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;
    private final MediaAssetRepository mediaAssetRepository;
    private final SlugGenerator slugGenerator;

    private final AtomicBoolean bucketEnsured = new AtomicBoolean(false);

    public MediaAsset storeMedia(MediaUploadCommand command) {
        ensureBucketExists();
        String objectKey = buildObjectKey(command);

        try (InputStream inputStream = command.inputStream()) {
            String contentType = resolveContentType(command);
            var putArgs = PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .stream(inputStream, command.fileSize(), -1)
                    .contentType(contentType)
                    .build();

            var response = minioClient.putObject(putArgs);

            MediaAsset asset = MediaAsset.create(
                    properties.getBucket(),
                    objectKey,
                    sanitizeFileName(command.fileName()),
                    contentType,
                    command.fileSize(),
                    command.category(),
                    response.etag(),
                    command.description()
            );

            return mediaAssetRepository.save(asset);
        } catch (Exception ex) {
            throw new MediaStorageException("Failed to store media object", ex);
        }
    }

    @Transactional(readOnly = true)
    public MediaAsset getAsset(UUID assetId) {
        return mediaAssetRepository.findById(assetId)
                .orElseThrow(() -> new DomainNotFoundException("Media asset not found: " + assetId));
    }

    @Transactional(readOnly = true)
    public Page<MediaAsset> listByCategory(MediaCategory category, Pageable pageable) {
        return mediaAssetRepository.findAllByCategory(category, pageable);
    }

    public void deleteAsset(UUID assetId) {
        MediaAsset asset = getAsset(assetId);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(asset.getBucket())
                    .object(asset.getObjectKey())
                    .build());
        } catch (Exception ex) {
            throw new MediaStorageException("Failed to remove object from storage", ex);
        }
        mediaAssetRepository.delete(asset);
    }

    @Transactional(readOnly = true)
    public String generatePresignedUrl(UUID assetId) {
        MediaAsset asset = getAsset(assetId);
        try {
            long expirySecondsLong = properties.presignedExpiry().toSeconds();
            long bounded = Math.min(expirySecondsLong, 60L * 60 * 24 * 7); // Max 7 days per MinIO/S3 contract
            int expirySeconds = Math.toIntExact(Math.max(bounded, 60)); // enforce minimum 60 seconds
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(asset.getBucket())
                            .object(asset.getObjectKey())
                            .expiry(expirySeconds)
                            .method(Method.GET)
                            .build()
            );
        } catch (Exception ex) {
            throw new MediaStorageException("Failed to generate presigned URL", ex);
        }
    }

    private void ensureBucketExists() {
        if (bucketEnsured.get()) {
            return;
        }
        synchronized (bucketEnsured) {
            if (bucketEnsured.get()) {
                return;
            }
            try {
                boolean exists = minioClient.bucketExists(
                        BucketExistsArgs.builder().bucket(properties.getBucket()).build());
                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder()
                            .bucket(properties.getBucket())
                            .build());
                }
                bucketEnsured.set(true);
            } catch (Exception ex) {
                throw new MediaStorageException("Failed to verify or create MinIO bucket", ex);
            }
        }
    }

    private String buildObjectKey(MediaUploadCommand command) {
        String sanitizedName = slugGenerator.generateSlug(extractBaseName(command.fileName()));
        String extension = extractExtension(command.fileName());
        return "%s/%s-%s.%s".formatted(
                command.category().name().toLowerCase(Locale.ROOT),
                sanitizedName,
                UUID.randomUUID(),
                extension
        );
    }

    private String resolveContentType(MediaUploadCommand command) {
        return command.contentType() != null && !command.contentType().isBlank()
                ? command.contentType()
                : "application/octet-stream";
    }

    private String sanitizeFileName(String fileName) {
        return fileName != null && !fileName.isBlank() ? fileName.trim() : "upload";
    }

    private String extractBaseName(String fileName) {
        String sanitized = sanitizeFileName(fileName);
        int index = sanitized.lastIndexOf('.');
        if (index > 0) {
            return sanitized.substring(0, index);
        }
        return sanitized;
    }

    private String extractExtension(String fileName) {
        String sanitized = sanitizeFileName(fileName);
        int index = sanitized.lastIndexOf('.');
        if (index > 0 && index < sanitized.length() - 1) {
            return sanitized.substring(index + 1).toLowerCase(Locale.ROOT);
        }
        return "bin";
    }
}
