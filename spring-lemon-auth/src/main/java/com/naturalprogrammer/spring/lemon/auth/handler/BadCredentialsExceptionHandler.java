package com.naturalprogrammer.spring.lemon.auth.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;

import com.naturalprogrammer.spring.lemon.exceptions.handlers.AbstractExceptionHandler;

//@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class BadCredentialsExceptionHandler extends AbstractExceptionHandler<BadCredentialsException> {
	
	public BadCredentialsExceptionHandler() {
		
		super(BadCredentialsException.class);
		log.info("Created");
	}
	
	@Override
	public HttpStatus getStatus(BadCredentialsException ex) {
		return HttpStatus.UNAUTHORIZED;
	}
}
