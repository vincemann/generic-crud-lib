package com.naturalprogrammer.spring.lemon.auth.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import lemon.exceptions.handlers.AbstractExceptionHandler;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class UsernameNotFoundExceptionHandler extends AbstractExceptionHandler<UsernameNotFoundException> {
	
	public UsernameNotFoundExceptionHandler() {
		
		super(UsernameNotFoundException.class);
		log.info("Created");
	}
	
	@Override
	public HttpStatus getStatus(UsernameNotFoundException ex) {
		return HttpStatus.UNAUTHORIZED;
	}
}
