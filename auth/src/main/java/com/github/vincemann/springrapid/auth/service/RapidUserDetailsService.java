package com.github.vincemann.springrapid.auth.service;

import com.github.vincemann.springrapid.core.Root;
import com.github.vincemann.springrapid.auth.model.AbstractUser;
import com.github.vincemann.springrapid.auth.sec.AuthenticatedPrincipalFactory;



import com.github.vincemann.springrapid.core.sec.RapidPrincipal;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import com.github.vincemann.springrapid.core.util.Message;
import com.github.vincemann.springrapid.core.util.VerifyEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * UserDetailsService, as required by Spring Security.
 * 
 */

@Slf4j
public class RapidUserDetailsService
		implements UserDetailsService {

	private UserService userService;
	private AuthenticatedPrincipalFactory authenticatedPrincipalFactory;


	@Transactional
	@Override
	public RapidPrincipal loadUserByUsername(String contactInformation) throws UsernameNotFoundException {
		AbstractUser<?> user;
		try {
			Optional<AbstractUser<?>> byContactInformation = userService.findByContactInformation(contactInformation);
			VerifyEntity.isPresent(byContactInformation,"User with contactInformation: "+contactInformation+" not found");
			user = byContactInformation.get();
		} catch (EntityNotFoundException e) {
			throw new UsernameNotFoundException(
					Message.get("com.github.vincemann.userNotFound", contactInformation)
					,e);
		}

		return authenticatedPrincipalFactory.create(user);
	}

	@Autowired
	public void setPrincipalUserConverter(AuthenticatedPrincipalFactory authenticatedPrincipalFactory) {
		this.authenticatedPrincipalFactory = authenticatedPrincipalFactory;
	}

	@Autowired
	@Root
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}
