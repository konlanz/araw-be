package com.araw.media.domain.model;

import com.araw.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "media_assets")
public class MediaAsset extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bucket", nullable = false, length = 80)
    private String bucket;

    @Column(name = "object_key", nullable = false, unique = true, length = 255)
    private String objectKey;

    @Column(name = "file_name", nullable = false, length = 160)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 40)
    private MediaCategory category;

    @Column(name = "etag", length = 120)
    private String etag;

    @Column(name = "description", length = 240)
    private String description;

    protected MediaAsset(String bucket,
                         String objectKey,
                         String fileName,
                         String contentType,
                         long fileSize,
                         MediaCategory category,
                         String etag,
                         String description) {
        this.bucket = bucket;
        this.objectKey = objectKey;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.category = category;
        this.etag = etag;
        this.description = description;
    }

    public static MediaAsset create(String bucket,
                                    String objectKey,
                                    String fileName,
                                    String contentType,
                                    long fileSize,
                                    MediaCategory category,
                                    String etag,
                                    String description) {
        return new MediaAsset(bucket, objectKey, fileName, contentType, fileSize, category, etag, description);
    }

    public void updateDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }
}
