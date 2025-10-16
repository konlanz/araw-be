package com.araw.araw.domain.admin.entity;

import com.araw.araw.domain.admin.valueobject.AdminPermission;
import com.araw.araw.domain.admin.valueobject.AdminRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "title", length = 100)
    private String title; // Job title: "Program Director", "Event Coordinator", etc.

    @Column(name = "department", length = 50)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminRole role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "admin_permissions",
            joinColumns = @JoinColumn(name = "admin_id"))
    @Column(name = "permission")
    @Enumerated(EnumType.STRING)
    private Set<AdminPermission> permissions = new HashSet<>();

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_expiry")
    private LocalDateTime emailVerificationExpiry;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_expiry")
    private LocalDateTime passwordResetExpiry;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "timezone", length = 50)
    private String timezone = "America/New_York";

    @Column(name = "notification_preferences")
    private String notificationPreferences; // JSON string or comma-separated values

    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    // Audit fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private Admin createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by_id")
    private Admin lastModifiedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @Column(name = "deactivation_reason")
    private String deactivationReason;

    @Version
    private Long version;

    // ==================== Domain Methods ====================

    /**
     * Get full name of the admin
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if admin has a specific permission
     */
    public boolean hasPermission(AdminPermission permission) {
        // Super admin has all permissions
        if (role == AdminRole.SUPER_ADMIN) {
            return true;
        }
        // Check role-based permissions
        return role.getDefaultPermissions().contains(permission) ||
                permissions.contains(permission);
    }

    /**
     * Grant additional permission to admin
     */
    public void grantPermission(AdminPermission permission) {
        if (!hasPermission(permission)) {
            permissions.add(permission);
        }
    }

    /**
     * Revoke permission from admin
     */
    public void revokePermission(AdminPermission permission) {
        permissions.remove(permission);
    }

    /**
     * Check if admin can manage events
     */
    public boolean canManageEvents() {
        return hasPermission(AdminPermission.MANAGE_EVENTS);
    }

    /**
     * Check if admin can review applications
     */
    public boolean canReviewApplications() {
        return hasPermission(AdminPermission.REVIEW_APPLICATIONS);
    }

    /**
     * Check if admin can manage other admins
     */
    public boolean canManageAdmins() {
        return hasPermission(AdminPermission.MANAGE_ADMINS);
    }

    /**
     * Record successful login
     */
    public void recordSuccessfulLogin(String ipAddress) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    /**
     * Record failed login attempt
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.lockAccount(30); // Lock for 30 minutes after 5 failed attempts
        }
    }

    /**
     * Lock account for specified minutes
     */
    public void lockAccount(int minutes) {
        this.lockedUntil = LocalDateTime.now().plusMinutes(minutes);
    }

    /**
     * Check if account is locked
     */
    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Unlock account
     */
    public void unlockAccount() {
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    /**
     * Initiate password reset
     */
    public String initiatePasswordReset() {
        this.passwordResetToken = UUID.randomUUID().toString();
        this.passwordResetExpiry = LocalDateTime.now().plusHours(1);
        return this.passwordResetToken;
    }

    /**
     * Validate password reset token
     */
    public boolean isPasswordResetTokenValid(String token) {
        return passwordResetToken != null &&
                passwordResetToken.equals(token) &&
                passwordResetExpiry != null &&
                passwordResetExpiry.isAfter(LocalDateTime.now());
    }

    /**
     * Complete password reset
     */
    public void completePasswordReset(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.passwordResetToken = null;
        this.passwordResetExpiry = null;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    /**
     * Initiate email verification
     */
    public String initiateEmailVerification() {
        this.emailVerificationToken = UUID.randomUUID().toString();
        this.emailVerificationExpiry = LocalDateTime.now().plusDays(7);
        return this.emailVerificationToken;
    }

    /**
     * Verify email
     */
    public void verifyEmail(String token) {
        if (emailVerificationToken != null &&
                emailVerificationToken.equals(token) &&
                emailVerificationExpiry != null &&
                emailVerificationExpiry.isAfter(LocalDateTime.now())) {
            this.isEmailVerified = true;
            this.emailVerificationToken = null;
            this.emailVerificationExpiry = null;
        } else {
            throw new IllegalArgumentException("Invalid or expired verification token");
        }
    }

    /**
     * Activate admin account
     */
    public void activate() {
        if (this.isActive) {
            throw new IllegalStateException("Account is already active");
        }
        this.isActive = true;
        this.deactivatedAt = null;
        this.deactivationReason = null;
    }

    /**
     * Deactivate admin account
     */
    public void deactivate(String reason) {
        if (!this.isActive) {
            throw new IllegalStateException("Account is already inactive");
        }
        this.isActive = false;
        this.deactivatedAt = LocalDateTime.now();
        this.deactivationReason = reason;
    }

    /**
     * Change role
     */
    public void changeRole(AdminRole newRole, Admin changedBy) {
        if (this.role == newRole) {
            throw new IllegalStateException("Admin already has this role");
        }
        this.role = newRole;
        this.lastModifiedBy = changedBy;
        // Clear custom permissions when role changes
        this.permissions.clear();
    }

    /**
     * Update profile
     */
    public void updateProfile(String firstName, String lastName, String phoneNumber,
                              String title, String department, String bio) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.title = title;
        this.department = department;
        this.bio = bio;
    }

    /**
     * Check if admin can perform action on another admin
     */
    public boolean canModifyAdmin(Admin targetAdmin) {
        // Cannot modify yourself for certain operations
        if (this.id.equals(targetAdmin.getId())) {
            return false;
        }
        // Super admin can modify anyone
        if (this.role == AdminRole.SUPER_ADMIN) {
            return true;
        }
        // Admins can only modify lower-level roles
        return this.role.getLevel() < targetAdmin.getRole().getLevel();
    }
}
