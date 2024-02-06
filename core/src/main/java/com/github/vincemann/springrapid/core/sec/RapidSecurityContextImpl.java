package com.github.vincemann.springrapid.core.sec;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// use springs authentication.getDetails to store more info then already given in RapidAuthenticatedPrincipal
@Slf4j
public class RapidSecurityContextImpl implements RapidSecurityContext
{


    private AuthenticationManager authenticationManager;



    @Override
    public RapidPrincipal login(RapidPrincipal principal) {
        RapidPrincipal old = currentPrincipal();
        if (old != null) {
            if (log.isWarnEnabled())
                log.warn("Principal: " + old + " was already logged in. This login will override authenticated user");
        }
        Authentication auth = createToken(principal);
        Authentication authenticated = authenticationManager.authenticate(auth);
        SecurityContextHolder.getContext().setAuthentication(authenticated);
        return old;
    }


    @Override
    public RapidPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        try {
            return (RapidPrincipal) authentication.getPrincipal();
        }catch (ClassCastException e){
            throw new IllegalArgumentException("Security Context is malformed. Create with RapidSecurityContext.login(...)");
        }
    }

    @Override
    public void executeAsSystemUser(Runnable runnable) {
        if (systemUserAuthenticated()){
            runnable.run();
            return;
        }
        Authentication originalAuth = SecurityContextHolder.getContext().getAuthentication();
        try {
            SecurityContextHolder.getContext().setAuthentication(getSystemUser());
            runnable.run();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(originalAuth);
        }
    }

    @Override
    public <T> T executeAsSystemUser(Supplier<T> supplier) {
        if (systemUserAuthenticated())
            return supplier.get();
        Authentication originalAuth = SecurityContextHolder.getContext().getAuthentication();
        try {
            // dont go through authentication manager, bc system only exists in ram
            SecurityContextHolder.getContext().setAuthentication(getSystemUser());
            return supplier.get();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(originalAuth);
        }
    }

    protected boolean systemUserAuthenticated(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return false;
        if (authentication.getAuthorities() == null)
            return false;
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SYSTEM))){
            return true;
        }
        return false;
    }

    protected Authentication getSystemUser() {
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(Roles.SYSTEM);
        return new UsernamePasswordAuthenticationToken("system", null, authorities);
    }

    protected Authentication createToken(RapidPrincipal principal) {
        return new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
    }


    protected GrantedAuthority map(String role) {
        return new SimpleGrantedAuthority(role);
    }


    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}
