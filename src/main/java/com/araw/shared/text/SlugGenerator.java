package com.araw.shared.text;

import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
public class SlugGenerator {

    public String generateSlug(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String cleaned = normalized
                .replaceAll("[^\\p{Alnum}]+", "-")
                .replaceAll("^-+|-+$", "")
                .toLowerCase();
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("Unable to generate slug from input: " + input);
        }
        return cleaned;
    }
}
