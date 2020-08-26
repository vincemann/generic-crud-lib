package com.github.vincemann.springlemon.auth.util;

import com.github.vincemann.springlemon.auth.domain.AbstractUser;
import com.github.vincemann.springlemon.auth.service.token.JwtService;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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


//	/**
//	 * Signs a user in
//	 *
//	 * @param user
//	 */
//	public static <U extends AbstractUser<ID>, ID extends Serializable>
//	void login(U user) {
//
//		LemonAuthenticatedPrincipal principal = new LemonAuthenticatedPrincipal(user.toUserDto());
//
//		Authentication authentication = // make the authentication object
//	    	new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
//
//	    SecurityContextHolder.getContext().setAuthentication(authentication); // put that in the security context
//	    principal.eraseCredentials();
//	}
	
	
	/**
	 * Throws BadCredentialsException if 
	 * user's credentials were updated after the JWT was issued
	 */
	public static void ensureCredentialsUpToDate(JWTClaimsSet claims, AbstractUser<?> user) {
		
		long issueTime = claims.getIssueTime().getTime();

		LecUtils.ensureCredentials(issueTime >= user.getCredentialsUpdatedMillis(),
				"com.naturalprogrammer.spring.obsoleteToken");
	}
//
//	public static LemonUserDto getUserDto(JWTClaimsSet claims) {
//
//		Object userClaim = claims.getClaim(AuthorizationTokenService.USER_CLAIM);
//
//		if (userClaim == null)
//			return null;
//
//		return MapperUtils.deserialize((String) userClaim);
//	}
}
