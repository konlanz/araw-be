package com.araw.alumni.domain.model;

import com.araw.shared.exception.DomainValidationException;
import com.araw.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "alumni_class_gallery_items")
public class AlumniClassGalleryItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumni_class_id", nullable = false)
    private AlumniClass alumniClass;

    @Column(name = "media_id", nullable = false)
    private UUID mediaId;

    @Column(name = "caption", length = 160)
    private String caption;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public static AlumniClassGalleryItem create(UUID mediaId,
                                                String caption,
                                                Integer displayOrder) {
        AlumniClassGalleryItem item = new AlumniClassGalleryItem();
        item.setMediaId(mediaId);
        item.setCaption(caption);
        item.setDisplayOrder(displayOrder);
        return item;
    }

    void assignToClass(AlumniClass alumniClass) {
        this.alumniClass = alumniClass;
    }

    void detachFromClass() {
        this.alumniClass = null;
    }

    public void updateDetails(UUID mediaId,
                              String caption) {
        setMediaId(mediaId);
        setCaption(caption);
    }

    public void setDisplayOrder(Integer displayOrder) {
        if (displayOrder == null || displayOrder < 0) {
            throw new DomainValidationException("Display order must be a non-negative integer");
        }
        this.displayOrder = displayOrder;
    }

    private void setMediaId(UUID mediaId) {
        if (mediaId == null) {
            throw new DomainValidationException("Gallery item media id is required");
        }
        this.mediaId = mediaId;
    }

    private void setCaption(String caption) {
        this.caption = caption != null ? caption.trim() : null;
    }
}
