package com.araw.infrastructure.security.user;

import com.araw.araw.domain.admin.entity.Admin;
import com.araw.araw.domain.admin.valueobject.AdminPermission;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AdminUserDetails implements UserDetails {

    private final Admin admin;

    public AdminUserDetails(Admin admin) {
        this.admin = admin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name()));
        Set<AdminPermission> permissions = admin.getRole().getDefaultPermissions();
        if (admin.getPermissions() != null) {
            permissions.addAll(admin.getPermissions());
        }
        permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority("PERM_" + permission.name())));
        return authorities;
    }

    @Override
    public String getPassword() {
        return admin.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return admin.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !admin.isAccountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(admin.getIsActive());
    }

    public Admin getAdmin() {
        return admin;
    }
}
