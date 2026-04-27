package com.araw.infrastructure.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ResourceServerConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/swagger-ui/**",
            "/swagger-ui",
            "/swagger-ui/",
            "/swagger-ui.html",
            "/swagger/**",
            "/swagger/index.html",
            "/swagger",
            "/swagger/",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/actuator/**"
    };

    /**
     * Endpoints that are always public for GET (read-only data for the public-facing site).
     */
    private static final String[] PUBLIC_READ_ENDPOINTS = {
            "/api/public/**",
            "/api/articles/**",
            "/api/alumni/**",
            "/api/community/**",
            "/api/media/**",
            "/api/araw/events",
            "/api/araw/events/published",
            "/api/araw/events/*/gallery",
            "/api/araw/feedback/testimonials"
    };

    /**
     * HTTP methods that mutate state — these always require a valid JWT even on
     * otherwise "public" paths such as /api/alumni/** or /api/articles/**.
     */
    private static final HttpMethod[] WRITE_METHODS = {
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.PATCH,
            HttpMethod.DELETE
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Always allow: Swagger, Actuator, CORS pre-flight
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth endpoint — always requires JWT
                        .requestMatchers("/api/auth/**").authenticated()

                        // Write operations on public-read paths must still be authenticated
                        .requestMatchers(HttpMethod.POST,   "/api/alumni/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/alumni/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH,  "/api/alumni/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/alumni/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/articles/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/articles/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH,  "/api/articles/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/articles/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/community/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/community/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH,  "/api/community/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/community/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/media/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/media/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH,  "/api/media/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/media/**").authenticated()

                        // Public read-only API routes (GET only)
                        .requestMatchers(HttpMethod.GET, PUBLIC_READ_ENDPOINTS).permitAll()

                        // Everything else (admin APIs, mutations) requires authentication
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2Login(AbstractHttpConfigurer::disable)
                .oauth2Client(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                );
        return http.build();
    }
}
