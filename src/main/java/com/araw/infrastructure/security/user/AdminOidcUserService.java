package com.araw.infrastructure.security.user;

import com.araw.araw.domain.admin.entity.Admin;
import com.araw.araw.domain.admin.repository.AdminRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class AdminOidcUserService extends OidcUserService {

    private final AdminRepository adminRepository;

    public AdminOidcUserService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();
        Admin admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("No admin account mapped to " + email));
        if (!Boolean.TRUE.equals(admin.getIsActive())) {
            throw new BadCredentialsException("Admin account is inactive");
        }
        admin.recordSuccessfulLogin("google-oauth");
        adminRepository.save(admin);
        return oidcUser;
    }
}
