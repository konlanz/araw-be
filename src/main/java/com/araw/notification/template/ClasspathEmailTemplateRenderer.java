package com.araw.notification.template;

import com.araw.notification.config.EmailTemplateProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ClasspathEmailTemplateRenderer implements EmailTemplateRenderer {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_.-]+)\\s*}}");

    private final EmailTemplateProperties properties;
    private final ConcurrentHashMap<String, String> templateCache = new ConcurrentHashMap<>();

    public ClasspathEmailTemplateRenderer(EmailTemplateProperties properties) {
        this.properties = properties;
    }

    @Override
    public String render(String templateName, Map<String, ?> variables) {
        String templateContent = resolveTemplate(templateName);
        if (templateContent == null) {
            throw new IllegalArgumentException("Email template not found: " + templateName);
        }

        if (variables == null || variables.isEmpty()) {
            return templateContent;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(templateContent);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = findValueForKey(key, variables);
            String replacement = value != null ? Matcher.quoteReplacement(value.toString()) : "";
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private Object findValueForKey(String key, Map<String, ?> variables) {
        if (!key.contains(".")) {
            return variables.get(key);
        }
        String[] parts = key.split("\\.");
        Object current = variables;
        for (String part : parts) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    private String resolveTemplate(String templateName) {
        return templateCache.computeIfAbsent(templateName, this::loadTemplateFromClasspath);
    }

    private String loadTemplateFromClasspath(String templateName) {
        String normalized = templateName.startsWith("/") ? templateName.substring(1) : templateName;
        String fullPath = properties.getTemplateBasePath() + "/" + normalized;
        ClassPathResource resource = new ClassPathResource(fullPath);
        if (!resource.exists()) {
            return null;
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read email template: " + fullPath, ex);
        }
    }
}
