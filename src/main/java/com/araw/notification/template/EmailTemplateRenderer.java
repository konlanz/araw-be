package com.araw.notification.template;

import java.util.Map;

public interface EmailTemplateRenderer {

    /**
     * Render a template located under the configured template base path.
     *
     * @param templateName the template filename (e.g. "application-received.txt")
     * @param variables placeholder values referenced via {@code {{placeholder}}}
     * @return rendered string
     */
    String render(String templateName, Map<String, ?> variables);
}
