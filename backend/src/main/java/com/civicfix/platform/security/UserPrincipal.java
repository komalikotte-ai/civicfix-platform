package com.civicfix.platform.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final Long organizationId;
    private final Long departmentId;
    private final Long wardId;
    private final boolean active;
    private final boolean accountLocked;
    private final boolean emailVerified;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String username, String email, String password,
                         Long organizationId, Long departmentId, Long wardId,
                         boolean active, boolean accountLocked, boolean emailVerified,
                         Set<String> roles, Set<String> permissions,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.organizationId = organizationId;
        this.departmentId = departmentId;
        this.wardId = wardId;
        this.active = active;
        this.accountLocked = accountLocked;
        this.emailVerified = emailVerified;
        this.roles = roles;
        this.permissions = permissions;
        this.authorities = authorities;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public Long getWardId() {
        return wardId;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
