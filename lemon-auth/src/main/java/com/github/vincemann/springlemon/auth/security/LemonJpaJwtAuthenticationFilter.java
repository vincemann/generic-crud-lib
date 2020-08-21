package com.github.vincemann.springlemon.auth.security;

import com.github.vincemann.springlemon.auth.domain.AbstractUser;
import com.github.vincemann.springlemon.auth.domain.dto.user.LemonUserDto;
import com.github.vincemann.springlemon.auth.service.AuthorizationTokenService;
import com.github.vincemann.springlemon.auth.service.LemonUserDetailsService;
import com.github.vincemann.springlemon.auth.util.LemonUtils;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.Serializable;

//Einfach impl von Filter der Brücke zu jpa macht
public class LemonJpaJwtAuthenticationFilter<U extends AbstractUser<ID>, ID extends Serializable>
	extends LemonJwtAuthenticationFilter {

    private static final Log log = LogFactory.getLog(LemonJpaJwtAuthenticationFilter.class);

    private LemonUserDetailsService<U, ID> userDetailsService;
	
	public LemonJpaJwtAuthenticationFilter(AuthorizationTokenService authorizationTokenService,
										   LemonUserDetailsService<U, ID> userDetailsService) {
		
		super(authorizationTokenService);
		this.userDetailsService = userDetailsService;
		
		log.info("Created");		
	}

	////@LogInteraction(level = LogInteraction.Level.TRACE)
	@Override
	protected LemonUserDto fetchUserDto(JWTClaimsSet claims) {
        String username = claims.getSubject();
        U user = userDetailsService.findUserByEmail(username)
        		.orElseThrow(() -> new UsernameNotFoundException(username));

        log.debug("User found ...");

        LemonUtils.ensureCredentialsUpToDate(claims, user);
        LemonUserDto lemonUserDto = user.toUserDto();
        lemonUserDto.setPassword(null);
        
        return lemonUserDto;
	}
}
