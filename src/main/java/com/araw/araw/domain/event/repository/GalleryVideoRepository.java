package com.araw.araw.domain.event.repository;

import com.araw.araw.domain.event.entity.GalleryVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GalleryVideoRepository extends JpaRepository<GalleryVideo, UUID> {
}
