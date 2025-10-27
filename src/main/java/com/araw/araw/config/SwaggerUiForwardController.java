package com.araw.araw.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerUiForwardController {

    @GetMapping({"/swagger", "/swagger/", "/docs", "/docs/"})
    public String forwardToSwaggerUiRoot() {
        return "forward:/swagger-ui/index.html";
    }

    @GetMapping("/swagger/index.html")
    public String forwardToSwaggerUiIndex() {
        return "forward:/swagger-ui/index.html";
    }
}
