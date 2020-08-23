package com.github.vincemann.springrapid.core.security;

import lombok.Getter;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents logged in user.
 * Also contains {@link Authentication#getDetails()} information.
 */
@Getter
public class RapidAuthenticatedPrincipal implements AuthenticatedPrincipal, CredentialsContainer, UserDetails {
    private String name;
    private Set<String> roles;
    private String password;

    public RapidAuthenticatedPrincipal(String name, String password, Set<String> roles) {
        this.name = name;
        this.password = password;
        this.roles = roles;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setPassword(String password) {
        this.password = password;
    }

    protected void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public void eraseCredentials() {
        this.password =null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    // UserDetails ...

    @Override
    public String getPassword() {
        return getPassword();
    }

    @Override
    public String getName() {
        return getName();
    }

    @Override
    public String getUsername() {
        return getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}