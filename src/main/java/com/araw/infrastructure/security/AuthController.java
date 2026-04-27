package com.araw.infrastructure.security;

import com.araw.araw.domain.admin.entity.Admin;
import com.araw.araw.domain.admin.repository.AdminRepository;
import com.araw.infrastructure.security.dto.AdminProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints.
 *
 * <p>The frontend obtains a Google ID token via Google Identity Services (GIS),
 * then sends it as a Bearer token. This controller validates the JWT is a known
 * admin and returns the admin profile.</p>
 *
 * <pre>
 * Flow:
 *   1. Frontend → Google Sign-In button (Google Identity Services)
 *   2. Google  → returns credential (Google ID token / JWT)
 *   3. Frontend → GET /api/auth/me  with  Authorization: Bearer <id_token>
 *   4. Spring Security validates the JWT using GoogleOAuth2Config.googleJwtDecoder()
 *   5. This controller resolves the email claim and looks up the Admin record
 *   6. Returns AdminProfileResponse (id, name, role, permissions, …)
 * </pre>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Google OAuth2 authentication endpoints")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AdminRepository adminRepository;

    public AuthController(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    // -------------------------------------------------------------------------
    // GET /api/auth/me
    // -------------------------------------------------------------------------

    /**
     * Returns the currently authenticated admin's profile.
     *
     * <p>Requires a valid Google ID token in the {@code Authorization: Bearer} header.
     * Spring Security validates the JWT and injects the {@link Jwt} principal.
     * The admin is looked up by the {@code email} claim in the JWT.</p>
     *
     * @param jwt     the validated Google JWT injected by Spring Security
     * @param request the HTTP request (used to record IP for login audit)
     * @return 200 with {@link AdminProfileResponse}, or 401 / 403 if not authenticated
     */
    @GetMapping("/me")
    @Operation(
            summary = "Get current admin profile",
            description = "Returns the authenticated admin's profile. Requires a valid Google ID token.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AdminProfileResponse> getCurrentAdmin(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {

        String email = jwt.getClaimAsString("email");
        log.debug("Resolving admin profile for email: {}", email);

        Admin admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn("JWT presented for non-admin email: {}", email);
                    return new AdminNotFoundException(email);
                });

        if (!Boolean.TRUE.equals(admin.getIsActive())) {
            log.warn("Inactive admin attempted login: {}", email);
            throw new AdminInactiveException(email);
        }

        // Record the login for audit purposes
        String ipAddress = resolveClientIp(request);
        admin.recordSuccessfulLogin(ipAddress);
        adminRepository.save(admin);

        AdminProfileResponse profile = toResponse(admin);
        return ResponseEntity.ok(profile);
    }

    // -------------------------------------------------------------------------
    // POST /api/auth/logout  (stateless — just a client-side signal)
    // -------------------------------------------------------------------------

    /**
     * Logout endpoint. Because the backend is stateless (JWT-only), this endpoint
     * is a no-op — token invalidation happens on the client by discarding the token.
     * It exists so the frontend can call a consistent logout URL.
     */
    @PostMapping("/logout")
    @Operation(
            summary = "Logout (client-side)",
            description = "Stateless logout — the client should discard the Google credential. " +
                          "The backend has no session to invalidate."
    )
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private AdminProfileResponse toResponse(Admin admin) {
        return new AdminProfileResponse(
                admin.getId(),
                admin.getEmail(),
                admin.getUsername(),
                admin.getFirstName(),
                admin.getLastName(),
                admin.getFullName(),
                admin.getTitle(),
                admin.getDepartment(),
                admin.getProfilePictureUrl(),
                admin.getRole(),
                admin.getRole().getDisplayName(),
                admin.getRole().getDefaultPermissions(),
                admin.getLastLoginAt()
        );
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // -------------------------------------------------------------------------
    // Local exception types (handled by RestExceptionHandler)
    // -------------------------------------------------------------------------

    public static class AdminNotFoundException extends RuntimeException {
        public AdminNotFoundException(String email) {
            super("No admin account is mapped to Google account: " + email);
        }
    }

    public static class AdminInactiveException extends RuntimeException {
        public AdminInactiveException(String email) {
            super("Admin account is inactive: " + email);
        }
    }
}
