package com.araw.media.application.command;

import com.araw.media.domain.model.MediaCategory;

import java.io.IOException;
import java.io.InputStream;

public record MediaUploadCommand(
        String fileName,
        String contentType,
        long fileSize,
        InputStream inputStream,
        MediaCategory category,
        String description
) implements AutoCloseable {

    @Override
    public void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ignored) {
                // best effort close; swallowing IOException is acceptable here
            }
        }
    }
}
