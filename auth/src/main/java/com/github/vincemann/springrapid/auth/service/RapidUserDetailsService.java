package com.github.vincemann.springrapid.auth.service;

import com.github.vincemann.aoplog.api.AopLoggable;
import com.github.vincemann.aoplog.api.annotation.LogInteraction;
import com.github.vincemann.springrapid.auth.model.AbstractUser;
import com.github.vincemann.springrapid.auth.security.AuthenticatedPrincipalFactory;



import com.github.vincemann.springrapid.core.security.AuthenticatedPrincipalImpl;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import com.github.vincemann.springrapid.core.slicing.ServiceComponent;
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
@ServiceComponent
@Slf4j
public class RapidUserDetailsService
		implements UserDetailsService, AopLoggable {

	private UserService userService;
	//keep it typeless...
	private AuthenticatedPrincipalFactory authenticatedPrincipalFactory;


	@Transactional
	@LogInteraction
	@Override
	public AuthenticatedPrincipalImpl loadUserByUsername(String contactInformation) throws UsernameNotFoundException {
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
	public void injectPrincipalUserConverter(AuthenticatedPrincipalFactory authenticatedPrincipalFactory) {
		this.authenticatedPrincipalFactory = authenticatedPrincipalFactory;
	}


	@Autowired
	public void injectUserService(UserService userService) {
		this.userService = userService;
	}
}
