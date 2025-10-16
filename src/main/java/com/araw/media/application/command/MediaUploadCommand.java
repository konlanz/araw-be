package com.araw.media.application.command;

import com.araw.media.domain.model.MediaCategory;

import java.io.InputStream;

public record MediaUploadCommand(
        String fileName,
        String contentType,
        long fileSize,
        InputStream inputStream,
        MediaCategory category,
        String description
) {
}
