package com.araw.notification.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TemplatedEmailRequest {

    private final String templateName;
    private final String subjectTemplate;
    private final Map<String, ?> variables;
    private final List<String> to;
    private final List<String> cc;
    private final List<String> bcc;
    private final String from;

    private TemplatedEmailRequest(Builder builder) {
        this.templateName = Objects.requireNonNull(builder.templateName, "templateName is required");
        this.subjectTemplate = Objects.requireNonNull(builder.subjectTemplate, "subjectTemplate is required");
        this.variables = builder.variables;
        this.to = List.copyOf(builder.to);
        this.cc = List.copyOf(builder.cc);
        this.bcc = List.copyOf(builder.bcc);
        this.from = builder.from;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    public Map<String, ?> getVariables() {
        return variables;
    }

    public List<String> getTo() {
        return to;
    }

    public List<String> getCc() {
        return cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public String getFrom() {
        return from;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String templateName;
        private String subjectTemplate;
        private Map<String, ?> variables = Map.of();
        private final List<String> to = new ArrayList<>();
        private final List<String> cc = new ArrayList<>();
        private final List<String> bcc = new ArrayList<>();
        private String from;

        private Builder() {
        }

        public Builder templateName(String templateName) {
            this.templateName = templateName;
            return this;
        }

        public Builder subjectTemplate(String subjectTemplate) {
            this.subjectTemplate = subjectTemplate;
            return this;
        }

        public Builder variables(Map<String, ?> variables) {
            if (variables != null) {
                this.variables = variables;
            }
            return this;
        }

        public Builder to(List<String> recipients) {
            if (recipients != null) {
                recipients.stream()
                        .filter(address -> address != null && !address.isBlank())
                        .map(String::trim)
                        .forEach(this.to::add);
            }
            return this;
        }

        public Builder to(String... recipients) {
            if (recipients != null) {
                for (String recipient : recipients) {
                    if (recipient != null && !recipient.isBlank()) {
                        this.to.add(recipient.trim());
                    }
                }
            }
            return this;
        }

        public Builder cc(List<String> recipients) {
            if (recipients != null) {
                recipients.stream()
                        .filter(address -> address != null && !address.isBlank())
                        .map(String::trim)
                        .forEach(this.cc::add);
            }
            return this;
        }

        public Builder bcc(List<String> recipients) {
            if (recipients != null) {
                recipients.stream()
                        .filter(address -> address != null && !address.isBlank())
                        .map(String::trim)
                        .forEach(this.bcc::add);
            }
            return this;
        }

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public TemplatedEmailRequest build() {
            if (this.to.isEmpty()) {
                throw new IllegalArgumentException("At least one recipient is required");
            }
            return new TemplatedEmailRequest(this);
        }
    }
}
