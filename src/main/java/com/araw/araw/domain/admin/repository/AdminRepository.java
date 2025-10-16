package com.araw.araw.domain.admin.repository;

import com.araw.araw.domain.admin.entity.Admin;
import com.araw.araw.domain.admin.valueobject.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {

    Optional<Admin> findByUsername(String username);

    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByEmailIgnoreCase(String email);

    Optional<Admin> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);

    List<Admin> findByRole(AdminRole role);

    List<Admin> findByRoleIn(List<AdminRole> roles);

    List<Admin> findByIsActiveTrue();

    List<Admin> findByIsActiveFalse();

    List<Admin> findByIsEmailVerifiedTrue();

    List<Admin> findByIsEmailVerifiedFalse();

    Optional<Admin> findByPasswordResetToken(String token);

    Optional<Admin> findByEmailVerificationToken(String token);

    @Query("SELECT a FROM Admin a WHERE a.passwordResetToken = :token " +
            "AND a.passwordResetExpiry > :now")
    Optional<Admin> findByValidPasswordResetToken(@Param("token") String token,
                                                  @Param("now") LocalDateTime now);

    @Query("SELECT a FROM Admin a WHERE a.emailVerificationToken = :token " +
            "AND a.emailVerificationExpiry > :now")
    Optional<Admin> findByValidEmailVerificationToken(@Param("token") String token,
                                                      @Param("now") LocalDateTime now);

    @Query("SELECT a FROM Admin a WHERE a.lockedUntil IS NOT NULL AND a.lockedUntil > :now")
    List<Admin> findLockedAccounts(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Admin a SET a.lockedUntil = null, a.failedLoginAttempts = 0 " +
            "WHERE a.lockedUntil IS NOT NULL AND a.lockedUntil <= :now")
    int unlockExpiredAccounts(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM Admin a WHERE " +
            "LOWER(a.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Admin> searchAdmins(@Param("searchTerm") String searchTerm);

    @Query("SELECT a FROM Admin a WHERE a.lastLoginAt < :date")
    List<Admin> findInactiveAdminsSince(@Param("date") LocalDateTime date);

    @Query("SELECT a FROM Admin a WHERE a.lastLoginAt BETWEEN :startDate AND :endDate")
    List<Admin> findAdminsActiveInPeriod(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM Admin a WHERE a.role = :role AND a.isActive = true")
    long countActiveAdminsByRole(@Param("role") AdminRole role);

    @Query("SELECT a.role, COUNT(a) FROM Admin a WHERE a.isActive = true GROUP BY a.role")
    List<Object[]> countAdminsByRole();

    List<Admin> findByDepartment(String department);

    @Query("SELECT DISTINCT a.department FROM Admin a WHERE a.department IS NOT NULL")
    List<String> findAllDepartments();

    List<Admin> findByTwoFactorEnabledTrue();

    Optional<Admin> findByTwoFactorSecret(String secret);
}
