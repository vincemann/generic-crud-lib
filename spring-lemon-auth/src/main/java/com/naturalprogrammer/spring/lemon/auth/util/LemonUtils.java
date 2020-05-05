package com.naturalprogrammer.spring.lemon.auth.util;

import com.naturalprogrammer.spring.lemon.auth.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.auth.security.domain.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.auth.security.domain.LemonUserDto;
import com.naturalprogrammer.spring.lemon.auth.security.service.BlueTokenService;
import com.naturalprogrammer.spring.lemon.auth.security.service.LemonTokenService;
import com.nimbusds.jwt.JWTClaimsSet;
import io.github.vincemann.springrapid.core.util.MapperUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;

/**
 * Useful helper methods
 * 
 * @author Sanjay Patel
 */
public class LemonUtils {
	
	private static final Log log = LogFactory.getLog(LemonUtils.class);

	public LemonUtils() {
		
		log.info("Created");
	}


	/**
	 * Signs a user in
	 * 
	 * @param user
	 */
	public static <U extends AbstractUser<ID>, ID extends Serializable>
	void login(U user) {
		
		LemonPrincipal principal = new LemonPrincipal(user.toUserDto());

		Authentication authentication = // make the authentication object
	    	new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

	    SecurityContextHolder.getContext().setAuthentication(authentication); // put that in the security context
	    principal.eraseCredentials();
	}
	
	
	/**
	 * Throws BadCredentialsException if 
	 * user's credentials were updated after the JWT was issued
	 */
	public static <U extends AbstractUser<ID>, ID extends Serializable>
	void ensureCredentialsUpToDate(JWTClaimsSet claims, U user) {
		
		long issueTime = (long) claims.getClaim(LemonTokenService.LEMON_IAT);

		LecUtils.ensureCredentials(issueTime >= user.getCredentialsUpdatedMillis(),
				"com.naturalprogrammer.spring.obsoleteToken");
	}

	public static LemonUserDto getUserDto(JWTClaimsSet claims) {

		Object userClaim = claims.getClaim(BlueTokenService.USER_CLAIM);

		if (userClaim == null)
			return null;

		return MapperUtils.deserialize((String) userClaim);
	}
}
