package com.araw.alumni.domain.model;

import com.araw.shared.exception.DomainValidationException;
import com.araw.shared.persistence.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "alumni_classes")
public class AlumniClass extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "graduation_year", nullable = false)
    private Integer graduationYear;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "cover_media_id")
    private UUID coverMediaId;

    @OneToMany(mappedBy = "alumniClass", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC, createdAt ASC")
    private final List<AlumniClassStudent> students = new ArrayList<>();

    @OneToMany(mappedBy = "alumniClass", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC, createdAt ASC")
    private final List<AlumniClassGalleryItem> galleryItems = new ArrayList<>();

    public static AlumniClass create(String name,
                                     Integer graduationYear,
                                     String description,
                                     UUID coverMediaId) {
        AlumniClass alumniClass = new AlumniClass();
        alumniClass.setName(name);
        alumniClass.setGraduationYear(graduationYear);
        alumniClass.setDescription(description);
        alumniClass.coverMediaId = coverMediaId;
        return alumniClass;
    }

    public void updateDetails(String name,
                              Integer graduationYear,
                              String description,
                              UUID coverMediaId) {
        setName(name);
        setGraduationYear(graduationYear);
        setDescription(description);
        this.coverMediaId = coverMediaId;
    }

    public void addStudent(AlumniClassStudent student) {
        Objects.requireNonNull(student, "Student must not be null");
        student.assignToClass(this);
        students.add(student);
    }

    public void removeStudent(AlumniClassStudent student) {
        if (students.remove(student)) {
            student.detachFromClass();
        }
    }

    public void reorderStudents(List<AlumniClassStudent> desiredOrder) {
        Map<AlumniClassStudent, Integer> orderMap = new IdentityHashMap<>();
        for (int index = 0; index < desiredOrder.size(); index++) {
            orderMap.put(desiredOrder.get(index), index);
        }
        students.sort((left, right) -> {
            Integer leftIndex = orderMap.getOrDefault(left, Integer.MAX_VALUE);
            Integer rightIndex = orderMap.getOrDefault(right, Integer.MAX_VALUE);
            return Integer.compare(leftIndex, rightIndex);
        });
    }

    public void addGalleryItem(AlumniClassGalleryItem galleryItem) {
        Objects.requireNonNull(galleryItem, "Gallery item must not be null");
        galleryItem.assignToClass(this);
        galleryItems.add(galleryItem);
    }

    public void removeGalleryItem(AlumniClassGalleryItem galleryItem) {
        if (galleryItems.remove(galleryItem)) {
            galleryItem.detachFromClass();
        }
    }

    public void reorderGalleryItems(List<AlumniClassGalleryItem> desiredOrder) {
        Map<AlumniClassGalleryItem, Integer> orderMap = new IdentityHashMap<>();
        for (int index = 0; index < desiredOrder.size(); index++) {
            orderMap.put(desiredOrder.get(index), index);
        }
        galleryItems.sort((left, right) -> {
            Integer leftIndex = orderMap.getOrDefault(left, Integer.MAX_VALUE);
            Integer rightIndex = orderMap.getOrDefault(right, Integer.MAX_VALUE);
            return Integer.compare(leftIndex, rightIndex);
        });
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("Class name must not be blank");
        }
        this.name = name.trim();
    }

    public void setGraduationYear(Integer graduationYear) {
        if (graduationYear == null) {
            throw new DomainValidationException("Graduation year is required");
        }
        if (graduationYear < 1900 || graduationYear > 3000) {
            throw new DomainValidationException("Graduation year must be between 1900 and 3000");
        }
        this.graduationYear = graduationYear;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }
}
