package io.github.vincemann.spring.lemon.exceptions.handlers;

import java.util.Collection;

import io.github.vincemann.spring.lemon.exceptions.LemonFieldError;
import io.github.vincemann.spring.lemon.exceptions.MultiErrorException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;

//@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class MultiErrorExceptionHandler extends AbstractExceptionHandler<MultiErrorException> {

	public MultiErrorExceptionHandler() {
		
		super(MultiErrorException.class);
		log.info("Created");
	}
	
	@Override
	public String getExceptionId(MultiErrorException ex) {
		
		if (ex.getExceptionId() == null)
			return super.getExceptionId(ex);
		
		return ex.getExceptionId();
	}

	@Override
	public String getMessage(MultiErrorException ex) {
		return ex.getMessage();
	}

	@Override
	public HttpStatus getStatus(MultiErrorException ex) {
		return ex.getStatus();
	}
	
	@Override
	public Collection<LemonFieldError> getErrors(MultiErrorException ex) {
		return ex.getErrors();
	}
}
