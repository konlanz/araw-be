package com.araw.araw.domain.admin.service;

import com.araw.araw.application.dto.admin.AdminStatistics;
import com.araw.araw.application.dto.admin.DepartmentStats;
import com.araw.araw.domain.admin.entity.Admin;
import com.araw.araw.domain.admin.repository.AdminRepository;
import com.araw.araw.domain.admin.valueobject.AdminPermission;
import com.araw.araw.domain.admin.valueobject.AdminRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminDomainService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== ADMIN CREATION & MANAGEMENT ====================

    /**
     * Create a new admin with validation
     */
    public Admin createAdmin(Admin admin, Admin createdBy) {
        // Validate creator has permission
        if (!createdBy.hasPermission(AdminPermission.MANAGE_ADMINS)) {
            throw new SecurityException("Insufficient permissions to create admin");
        }

        // Check for duplicate username/email
        if (adminRepository.existsByUsername(admin.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (adminRepository.existsByEmail(admin.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Only super admin can create another super admin
        if (admin.getRole() == AdminRole.SUPER_ADMIN &&
                createdBy.getRole() != AdminRole.SUPER_ADMIN) {
            throw new SecurityException("Only super admin can create another super admin");
        }

        // Set audit fields
        admin.setCreatedBy(createdBy);
        admin.setPasswordHash(passwordEncoder.encode(admin.getPasswordHash()));
        admin.setEmailVerificationToken(admin.initiateEmailVerification());

        Admin saved = adminRepository.save(admin);

        log.info("Admin created: {} by {}", saved.getUsername(), createdBy.getUsername());

        return saved;
    }

    /**
     * Update admin profile with duplicate check
     */
    public Admin updateAdminProfile(UUID adminId, Admin updates, Admin updatedBy) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        // Check if updating own profile or has permission
        if (!admin.getId().equals(updatedBy.getId()) &&
                !updatedBy.hasPermission(AdminPermission.MANAGE_ADMINS)) {
            throw new SecurityException("Insufficient permissions to update admin");
        }

        // Check username availability if changing
        if (!admin.getUsername().equals(updates.getUsername()) &&
                !isUsernameAvailable(updates.getUsername(), adminId)) {
            throw new IllegalArgumentException("Username already taken");
        }

        // Check email availability if changing
        if (!admin.getEmail().equals(updates.getEmail()) &&
                !isEmailAvailable(updates.getEmail(), adminId)) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Update profile fields
        admin.updateProfile(
                updates.getFirstName(),
                updates.getLastName(),
                updates.getPhoneNumber(),
                updates.getTitle(),
                updates.getDepartment(),
                updates.getBio()
        );

        admin.setLastModifiedBy(updatedBy);

        log.info("Admin {} profile updated by {}", admin.getUsername(), updatedBy.getUsername());

        return adminRepository.save(admin);
    }

    // ==================== ROLE & PERMISSION MANAGEMENT ====================

    /**
     * Update admin role with hierarchy validation
     */
    public Admin updateAdminRole(UUID adminId, AdminRole newRole, Admin modifiedBy) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        // Validate modifier can change this admin's role
        if (!modifiedBy.canModifyAdmin(admin)) {
            throw new SecurityException("Cannot modify admin with equal or higher role level");
        }

        // Prevent demoting the last super admin
        if (admin.getRole() == AdminRole.SUPER_ADMIN && newRole != AdminRole.SUPER_ADMIN) {
            long superAdminCount = adminRepository.countActiveAdminsByRole(AdminRole.SUPER_ADMIN);
            if (superAdminCount <= 1) {
                throw new IllegalStateException("Cannot demote the last super admin");
            }
        }

        AdminRole oldRole = admin.getRole();
        admin.changeRole(newRole, modifiedBy);

        log.info("Admin {} role changed from {} to {} by {}",
                admin.getUsername(), oldRole, newRole, modifiedBy.getUsername());

        return adminRepository.save(admin);
    }

    /**
     * Grant additional permissions to admin
     */
    public Admin grantPermissions(UUID adminId, Set<AdminPermission> permissions, Admin grantedBy) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        // Validate granter has permission to manage admins
        if (!grantedBy.hasPermission(AdminPermission.MANAGE_ADMINS)) {
            throw new SecurityException("Insufficient permissions to grant permissions");
        }

        // Cannot grant permissions to higher level admin
        if (!grantedBy.canModifyAdmin(admin)) {
            throw new SecurityException("Cannot modify permissions for equal or higher role");
        }

        // Validate permissions are appropriate for role
        Set<AdminPermission> inappropriatePermissions = permissions.stream()
                .filter(p -> !isPermissionAppropriateForRole(p, admin.getRole()))
                .collect(Collectors.toSet());

        if (!inappropriatePermissions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Permissions not appropriate for role: " + inappropriatePermissions
            );
        }

        permissions.forEach(admin::grantPermission);
        admin.setLastModifiedBy(grantedBy);

        log.info("Permissions {} granted to {} by {}",
                permissions, admin.getUsername(), grantedBy.getUsername());

        return adminRepository.save(admin);
    }

    /**
     * Revoke permissions from admin
     */
    public Admin revokePermissions(UUID adminId, Set<AdminPermission> permissions, Admin revokedBy) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        if (!revokedBy.hasPermission(AdminPermission.MANAGE_ADMINS)) {
            throw new SecurityException("Insufficient permissions to revoke permissions");
        }

        if (!revokedBy.canModifyAdmin(admin)) {
            throw new SecurityException("Cannot modify permissions for equal or higher role");
        }

        permissions.forEach(admin::revokePermission);
        admin.setLastModifiedBy(revokedBy);

        log.info("Permissions {} revoked from {} by {}",
                permissions, admin.getUsername(), revokedBy.getUsername());

        return adminRepository.save(admin);
    }

    // ==================== AUTHENTICATION & SECURITY ====================

    /**
     * Process login attempt with security measures (supports username or email)
     */
    public Admin processLogin(String usernameOrEmail, String password, String ipAddress) {
        // Try to find by username first, then by email
        Admin admin = adminRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> adminRepository.findByEmailIgnoreCase(usernameOrEmail)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials")));

        // Check if account is locked
        if (admin.isAccountLocked()) {
            throw new SecurityException("Account is locked. Please try again later.");
        }

        // Check if account is active
        if (!admin.getIsActive()) {
            throw new SecurityException("Account is deactivated");
        }

        // Verify password
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            admin.recordFailedLogin();
            adminRepository.save(admin);
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Check if email is verified
        if (!admin.getIsEmailVerified()) {
            throw new SecurityException("Email not verified. Please check your email.");
        }

        // Record successful login
        admin.recordSuccessfulLogin(ipAddress);

        log.info("Successful login for {} from IP {}", admin.getUsername(), ipAddress);

        return adminRepository.save(admin);
    }

    /**
     * Process password reset request
     */
    public String initiatePasswordReset(String email) {
        Admin admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        if (!admin.getIsActive()) {
            throw new SecurityException("Account is deactivated");
        }

        String resetToken = admin.initiatePasswordReset();
        adminRepository.save(admin);

        log.info("Password reset initiated for {}", admin.getUsername());

        return resetToken; // This should be sent via email, not returned directly in production
    }


    public Admin resetPassword(String token, String newPassword) {
        LocalDateTime now = LocalDateTime.now();
        Admin admin = adminRepository.findByValidPasswordResetToken(token, now)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        String hashedPassword = passwordEncoder.encode(newPassword);
        admin.completePasswordReset(hashedPassword);

        log.info("Password reset completed for {}", admin.getUsername());

        return adminRepository.save(admin);
    }

    public Admin verifyEmail(String token) {
        LocalDateTime now = LocalDateTime.now();
        Admin admin = adminRepository.findByValidEmailVerificationToken(token, now)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        admin.verifyEmail(token);

        log.info("Email verified for {}", admin.getUsername());

        return adminRepository.save(admin);
    }


    public Admin enableTwoFactorAuth(UUID adminId, String secret) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        // Check if secret is already in use
        if (adminRepository.findByTwoFactorSecret(secret).isPresent()) {
            throw new IllegalArgumentException("Two-factor secret already in use");
        }

        admin.setTwoFactorEnabled(true);
        admin.setTwoFactorSecret(secret);

        log.info("Two-factor authentication enabled for {}", admin.getUsername());

        return adminRepository.save(admin);
    }


    public Admin disableTwoFactorAuth(UUID adminId, String password) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        // Verify password before disabling 2FA
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new SecurityException("Invalid password");
        }

        admin.setTwoFactorEnabled(false);
        admin.setTwoFactorSecret(null);

        log.info("Two-factor authentication disabled for {}", admin.getUsername());

        return adminRepository.save(admin);
    }


    public List<Admin> getAdminsWithTwoFactorEnabled() {
        return adminRepository.findByTwoFactorEnabledTrue();
    }

    /**
     * Validate two-factor code
     */
    public boolean validateTwoFactorCode(UUID adminId, String code) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        if (!admin.getTwoFactorEnabled()) {
            return true; // 2FA not enabled, no validation needed
        }

        // TODO: Implement actual TOTP validation logic here
        // This would typically use a library like Google Authenticator

        return true;
    }


    public Admin deactivateAdmin(UUID adminId, String reason, Admin deactivatedBy) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        if (admin.getId().equals(deactivatedBy.getId())) {
            throw new IllegalStateException("Cannot deactivate your own account");
        }

        if (!deactivatedBy.canModifyAdmin(admin)) {
            throw new SecurityException("Cannot deactivate admin with equal or higher role");
        }

        if (admin.getRole() == AdminRole.SUPER_ADMIN) {
            long activeSuperAdminCount = adminRepository.countActiveAdminsByRole(AdminRole.SUPER_ADMIN);
            if (activeSuperAdminCount <= 1) {
                throw new IllegalStateException("Cannot deactivate the last super admin");
            }
        }

        admin.deactivate(reason);
        admin.setLastModifiedBy(deactivatedBy);

        log.info("Admin {} deactivated by {} for: {}",
                admin.getUsername(), deactivatedBy.getUsername(), reason);

        return adminRepository.save(admin);
    }


    public Admin reactivateAdmin(UUID adminId, Admin reactivatedBy) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        if (!reactivatedBy.hasPermission(AdminPermission.MANAGE_ADMINS)) {
            throw new SecurityException("Insufficient permissions to reactivate admin");
        }

        admin.activate();
        admin.unlockAccount();
        admin.setLastModifiedBy(reactivatedBy);

        log.info("Admin {} reactivated by {}", admin.getUsername(), reactivatedBy.getUsername());

        return adminRepository.save(admin);
    }


    public List<Admin> searchAdmins(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be empty");
        }
        return adminRepository.searchAdmins(searchTerm);
    }


    public Optional<Admin> findByUsernameOrEmail(String usernameOrEmail) {
        return adminRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
    }

    public List<Admin> getAdminsByRole(AdminRole role) {
        return adminRepository.findByRole(role);
    }


    public List<Admin> getAdminsByRoles(List<AdminRole> roles) {
        return adminRepository.findByRoleIn(roles);
    }


    public List<Admin> getActiveAdmins() {
        return adminRepository.findByIsActiveTrue();
    }


    public List<Admin> getInactiveAdmins() {
        return adminRepository.findByIsActiveFalse();
    }


    public List<Admin> getVerifiedAdmins() {
        return adminRepository.findByIsEmailVerifiedTrue();
    }


    public List<Admin> getUnverifiedAdmins() {
        return adminRepository.findByIsEmailVerifiedFalse();
    }


    public List<Admin> getAdminsByDepartment(String department) {
        return adminRepository.findByDepartment(department);
    }


    public List<String> getAllDepartments() {
        return adminRepository.findAllDepartments();
    }


    public Map<String, DepartmentStats> getDepartmentStatistics() {
        List<String> departments = adminRepository.findAllDepartments();
        Map<String, DepartmentStats> stats = new HashMap<>();

        for (String dept : departments) {
            List<Admin> deptAdmins = adminRepository.findByDepartment(dept);

            long activeCount = deptAdmins.stream().filter(Admin::getIsActive).count();
            long verifiedCount = deptAdmins.stream().filter(Admin::getIsEmailVerified).count();
            Map<AdminRole, Long> roleDistribution = deptAdmins.stream()
                    .collect(Collectors.groupingBy(Admin::getRole, Collectors.counting()));

            stats.put(dept, DepartmentStats.builder()
                    .departmentName(dept)
                    .totalAdmins((long) deptAdmins.size())
                    .activeAdmins(activeCount)
                    .verifiedAdmins(verifiedCount)
                    .roleDistribution(roleDistribution)
                    .build());
        }

        return stats;
    }

    public boolean isUsernameAvailable(String username, UUID excludeId) {
        if (excludeId != null) {
            return !adminRepository.existsByUsernameAndIdNot(username, excludeId);
        }
        return !adminRepository.existsByUsername(username);
    }


    public boolean isEmailAvailable(String email, UUID excludeId) {
        if (excludeId != null) {
            return !adminRepository.existsByEmailAndIdNot(email, excludeId);
        }
        return !adminRepository.existsByEmail(email);
    }


    public boolean checkPermission(UUID adminId, AdminPermission permission) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        return admin.getIsActive() && admin.getIsEmailVerified() && admin.hasPermission(permission);
    }


    public void validateAdminAccess(UUID adminId, AdminPermission requiredPermission) {
        if (!checkPermission(adminId, requiredPermission)) {
            throw new SecurityException("Insufficient permissions for this action");
        }
    }


    public List<Admin> findInactiveAdmins(int daysInactive) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
        return adminRepository.findInactiveAdminsSince(cutoffDate);
    }


    public List<Admin> findAdminsActiveInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return adminRepository.findAdminsActiveInPeriod(startDate, endDate);
    }


    public List<Admin> findLockedAccounts() {
        return adminRepository.findLockedAccounts(LocalDateTime.now());
    }

    public List<Admin> auditExcessivePermissions() {
        List<Admin> admins = adminRepository.findAll();
        List<Admin> flagged = new ArrayList<>();

        for (Admin admin : admins) {
            // Check if non-super admin has manage_admins permission
            if (admin.getRole() != AdminRole.SUPER_ADMIN &&
                    admin.hasPermission(AdminPermission.MANAGE_ADMINS)) {
                flagged.add(admin);
            }

            // Check if viewer has any non-view permissions
            if (admin.getRole() == AdminRole.VIEWER) {
                boolean hasNonViewPermission = admin.getPermissions().stream()
                        .anyMatch(p -> !p.name().startsWith("VIEW_"));
                if (hasNonViewPermission) {
                    flagged.add(admin);
                }
            }
        }

        return flagged;
    }


    public Map<String, List<Admin>> findPotentialDuplicates() {
        List<Admin> allAdmins = adminRepository.findAll();
        Map<String, List<Admin>> duplicates = new HashMap<>();

        Map<String, List<Admin>> byFullName = allAdmins.stream()
                .collect(Collectors.groupingBy(admin ->
                        (admin.getFirstName() + " " + admin.getLastName()).toLowerCase()
                ));

        byFullName.forEach((name, admins) -> {
            if (admins.size() > 1) {
                duplicates.put("name: " + name, admins);
            }
        });

        return duplicates;
    }


    public int sendVerificationReminders() {
        List<Admin> unverified = adminRepository.findByIsEmailVerifiedFalse();

        int remindersSent = 0;
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);

        for (Admin admin : unverified) {
            if (admin.getCreatedAt().isBefore(cutoff) && admin.getIsActive()) {
                // Regenerate verification token if expired
                if (admin.getEmailVerificationExpiry() == null ||
                        admin.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
                    admin.initiateEmailVerification();
                    adminRepository.save(admin);
                    // TODO: Send email with new token
                    remindersSent++;
                }
            }
        }

        log.info("Sent {} verification reminder emails", remindersSent);
        return remindersSent;
    }


    public Admin transferSuperAdminRole(UUID fromAdminId, UUID toAdminId, String password) {
        Admin fromAdmin = adminRepository.findById(fromAdminId)
                .orElseThrow(() -> new IllegalArgumentException("Current super admin not found"));

        Admin toAdmin = adminRepository.findById(toAdminId)
                .orElseThrow(() -> new IllegalArgumentException("Target admin not found"));

        if (fromAdmin.getRole() != AdminRole.SUPER_ADMIN) {
            throw new SecurityException("Only super admin can transfer super admin role");
        }

        if (!passwordEncoder.matches(password, fromAdmin.getPasswordHash())) {
            throw new SecurityException("Invalid password");
        }

        if (!toAdmin.getIsActive() || !toAdmin.getIsEmailVerified()) {
            throw new IllegalStateException("Target admin must be active and email verified");
        }

        toAdmin.changeRole(AdminRole.SUPER_ADMIN, fromAdmin);
        fromAdmin.changeRole(AdminRole.ADMIN, toAdmin);

        adminRepository.saveAll(Arrays.asList(fromAdmin, toAdmin));

        log.warn("SUPER ADMIN ROLE TRANSFERRED from {} to {}",
                fromAdmin.getUsername(), toAdmin.getUsername());

        return toAdmin;
    }

    public AdminStatistics getAdminStatistics() {
        List<Object[]> roleDistribution = adminRepository.countAdminsByRole();
        Map<AdminRole, Long> roleCount = new HashMap<>();

        for (Object[] row : roleDistribution) {
            AdminRole role = (AdminRole) row[0];
            Long count = (Long) row[1];
            roleCount.put(role, count);
        }

        return AdminStatistics.builder()
                .totalAdmins(adminRepository.count())
                .activeAdmins((long) adminRepository.findByIsActiveTrue().size())
                .inactiveAdmins((long) adminRepository.findByIsActiveFalse().size())
                .verifiedAdmins((long) adminRepository.findByIsEmailVerifiedTrue().size())
                .unverifiedAdmins((long) adminRepository.findByIsEmailVerifiedFalse().size())
                .lockedAccounts((long) adminRepository.findLockedAccounts(LocalDateTime.now()).size())
                .twoFactorEnabledCount((long) adminRepository.findByTwoFactorEnabledTrue().size())
                .adminsByRole(roleCount)
                .departmentCount((long) adminRepository.findAllDepartments().size())
                .recentlyActive((long) adminRepository.findAdminsActiveInPeriod(
                        LocalDateTime.now().minusDays(7),
                        LocalDateTime.now()
                ).size())
                .build();
    }


    public Map<AdminRole, List<AdminRole>> getAdminHierarchy() {
        Map<AdminRole, List<AdminRole>> hierarchy = new HashMap<>();

        hierarchy.put(AdminRole.SUPER_ADMIN, Arrays.asList(AdminRole.values()));
        hierarchy.put(AdminRole.ADMIN, Arrays.asList(
                AdminRole.EVENT_MANAGER,
                AdminRole.APPLICATION_REVIEWER,
                AdminRole.CONTENT_MODERATOR,
                AdminRole.VIEWER
        ));
        hierarchy.put(AdminRole.EVENT_MANAGER, Arrays.asList(AdminRole.VIEWER));
        hierarchy.put(AdminRole.APPLICATION_REVIEWER, Arrays.asList(AdminRole.VIEWER));
        hierarchy.put(AdminRole.CONTENT_MODERATOR, Arrays.asList(AdminRole.VIEWER));
        hierarchy.put(AdminRole.VIEWER, Collections.emptyList());

        return hierarchy;
    }


    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    public void unlockExpiredAccounts() {
        int unlockedCount = adminRepository.unlockExpiredAccounts(LocalDateTime.now());
        if (unlockedCount > 0) {
            log.info("Unlocked {} expired admin accounts", unlockedCount);
        }
    }


    @Scheduled(cron = "0 0 2 * * *") // Run daily at 2 AM
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        List<Admin> admins = adminRepository.findAll();

        int cleaned = 0;
        for (Admin admin : admins) {
            boolean modified = false;

            if (admin.getPasswordResetToken() != null &&
                    admin.getPasswordResetExpiry() != null &&
                    admin.getPasswordResetExpiry().isBefore(now)) {
                admin.setPasswordResetToken(null);
                admin.setPasswordResetExpiry(null);
                modified = true;
            }

            if (admin.getEmailVerificationToken() != null &&
                    admin.getEmailVerificationExpiry() != null &&
                    admin.getEmailVerificationExpiry().isBefore(now)) {
                admin.setEmailVerificationToken(null);
                admin.setEmailVerificationExpiry(null);
                modified = true;
            }

            if (modified) {
                adminRepository.save(admin);
                cleaned++;
            }
        }

        if (cleaned > 0) {
            log.info("Cleaned expired tokens for {} admins", cleaned);
        }
    }


    @Scheduled(cron = "0 0 3 ? * SUN")
    public void performSecurityAudit() {
        log.info("Starting weekly security audit");

        List<Admin> excessivePermissions = auditExcessivePermissions();
        if (!excessivePermissions.isEmpty()) {
            log.warn("Found {} admins with potentially excessive permissions",
                    excessivePermissions.size());
        }

        List<Admin> inactiveAdmins = findInactiveAdmins(30);
        if (!inactiveAdmins.isEmpty()) {
            log.info("Found {} admins inactive for 30+ days", inactiveAdmins.size());
        }

        Map<String, List<Admin>> duplicates = findPotentialDuplicates();
        if (!duplicates.isEmpty()) {
            log.warn("Found {} potential duplicate admin groups", duplicates.size());
        }

        log.info("Weekly security audit completed");
    }

    private boolean isPermissionAppropriateForRole(AdminPermission permission, AdminRole role) {
        // Super admin can have any permission
        if (role == AdminRole.SUPER_ADMIN) {
            return true;
        }

        Set<AdminPermission> defaultPermissions = role.getDefaultPermissions();

        if (role == AdminRole.VIEWER) {
            return permission.name().startsWith("VIEW_");
        }

        if (role == AdminRole.CONTENT_MODERATOR) {
            return permission == AdminPermission.MANAGE_FEEDBACK ||
                    permission == AdminPermission.PUBLISH_TESTIMONIALS ||
                    permission.name().startsWith("VIEW_");
        }

        return defaultPermissions.contains(permission) ||
                permission.name().startsWith("VIEW_");
    }
}