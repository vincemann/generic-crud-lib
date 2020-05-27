package io.github.spring.lemon.auth.security.service;

import io.github.spring.lemon.auth.domain.AbstractUser;
import io.github.spring.lemon.auth.domain.AbstractUserRepository;
import io.github.spring.lemon.auth.security.domain.LemonPrincipal;
import io.github.vincemann.spring.lemon.exceptions.util.LexUtils;
import io.github.vincemann.springrapid.core.slicing.components.ServiceComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.Serializable;
import java.util.Optional;

/**
 * UserDetailsService, as required by Spring Security.
 * 
 * @author Sanjay Patel
 */
@ServiceComponent
public class LemonUserDetailsService
	<U extends AbstractUser<ID>, ID extends Serializable>
implements UserDetailsService {

	private static final Log log = LogFactory.getLog(LemonUserDetailsService.class);

	private final AbstractUserRepository<U,ID> userRepository;
	
	public LemonUserDetailsService(AbstractUserRepository<U, ID> userRepository) {
		
		this.userRepository = userRepository;
		log.info("Created");
	}
	
	@Override
	public LemonPrincipal loadUserByUsername(String username)
			throws UsernameNotFoundException {
		
		log.debug("Loading user having username: " + username);
		
		// delegates to findUserByUsername
		U user = findUserByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException(
				LexUtils.getMessage("com.naturalprogrammer.spring.userNotFound", username)));

		log.debug("Loaded user having username: " + username);

		return new LemonPrincipal(user.toUserDto());
	}

	/**
	 * Finds a user by the given username. Override this
	 * if you aren't using email as the username.
	 */
	public Optional<U> findUserByUsername(String username) {
		return userRepository.findByEmail(username);
	}
}
