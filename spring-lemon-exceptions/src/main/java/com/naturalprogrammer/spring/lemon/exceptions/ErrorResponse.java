package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.Collection;

import lombok.Getter;
import lombok.Setter;

/**
 * Error DTO, to be sent as response body
 * in case of errors
 */
@Getter @Setter
public class ErrorResponse {
	
	private String exceptionId;
	private String error;
	private String message;
	private Integer status; // We'd need it as integer in JSON serialization
	private Collection<LemonFieldError> errors;
	
	public boolean incomplete() {
		
		return message == null || status == null;
	}
}
