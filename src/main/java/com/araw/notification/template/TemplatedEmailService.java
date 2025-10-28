package com.araw.notification.template;

import com.araw.notification.config.EmailTemplateProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplatedEmailService {

    private static final Logger log = LoggerFactory.getLogger(TemplatedEmailService.class);
    private static final Pattern SUBJECT_PLACEHOLDER_PATTERN =
            Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_.-]+)\\s*}}");

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final EmailTemplateRenderer renderer;
    private final EmailTemplateProperties templateProperties;

    public TemplatedEmailService(JavaMailSender mailSender,
                                 MailProperties mailProperties,
                                 EmailTemplateRenderer renderer,
                                 EmailTemplateProperties templateProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
        this.renderer = renderer;
        this.templateProperties = templateProperties;
    }

    public void send(TemplatedEmailRequest request) {
        if (!isMailConfigured()) {
            log.warn("Skipping email send for template {}: mail credentials are not configured",
                    request.getTemplateName());
            return;
        }

        String subject = renderSubject(request.getSubjectTemplate(), request.getVariables());
        String body = renderer.render(request.getTemplateName(), request.getVariables());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(subject);
        message.setText(body);
        message.setTo(request.getTo().toArray(String[]::new));

        if (!CollectionUtils.isEmpty(request.getCc())) {
            message.setCc(request.getCc().toArray(String[]::new));
        }

        if (!CollectionUtils.isEmpty(request.getBcc())) {
            message.setBcc(request.getBcc().toArray(String[]::new));
        }

        String from = request.getFrom();
        if (from == null || from.isBlank()) {
            from = mailProperties.getUsername();
        }
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }

        mailSender.send(message);
    }

    public EmailTemplateProperties getTemplateProperties() {
        return templateProperties;
    }

    private String renderSubject(String subjectTemplate, Map<String, ?> variables) {
        if (subjectTemplate == null || subjectTemplate.isBlank()) {
            return "";
        }
        if (variables == null || variables.isEmpty()) {
            return subjectTemplate;
        }
        Matcher matcher = SUBJECT_PLACEHOLDER_PATTERN.matcher(subjectTemplate);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = resolveNestedValue(key, variables);
            String replacement = value != null ? Matcher.quoteReplacement(value.toString()) : "";
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private Object resolveNestedValue(String key, Map<String, ?> variables) {
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

    private boolean isMailConfigured() {
        String host = mailProperties.getHost();
        String username = mailProperties.getUsername();
        return host != null && !host.isBlank() && username != null && !username.isBlank();
    }
}
