package com.github.vincemann.springlemon.auth.security;

import com.github.vincemann.springlemon.auth.domain.AbstractUser;
import com.github.vincemann.springlemon.auth.domain.LemonAuthenticatedPrincipal;
import com.github.vincemann.springlemon.auth.service.UserService;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Only stores email in token and fetches user args for principal lazily
 */
@Transactional
public class LemonJwtClaimsPrincipalConverter
            implements JwtClaimsPrincipalConverter<LemonAuthenticatedPrincipal> {

    private UserService<AbstractUser<?>, ?, ?> userService;

    @Autowired
    public LemonJwtClaimsPrincipalConverter(UserService<AbstractUser<?>, ?, ?> userService) {
        this.userService = userService;
    }

    @Override
    public Map<String,Object> toClaims(LemonAuthenticatedPrincipal user) {
        return LemonMapUtils.mapOf("email",user.getEmail());
    }


    @Override
    public LemonAuthenticatedPrincipal toPrincipal(Map<String,Object> claims) throws AuthenticationCredentialsNotFoundException {
        String email = (String) claims.get("email");
        if (email == null)
            throw new AuthenticationCredentialsNotFoundException("email claim of claims-set not found");
        try {
            AbstractUser<?> user = userService.findByEmail(email);
            return new LemonAuthenticatedPrincipal(user.getEmail(), user.getPassword(),user.getId().toString(), user.getRoles());
        } catch (EntityNotFoundException e) {
            throw new AuthenticationCredentialsNotFoundException("User with in token encoded email: " + email + " does not exist.", e);
        }
    }


}