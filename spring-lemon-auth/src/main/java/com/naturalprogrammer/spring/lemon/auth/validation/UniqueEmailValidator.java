package com.naturalprogrammer.spring.lemon.auth.validation;

import com.naturalprogrammer.spring.lemon.auth.domain.AbstractUserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


/**
 * Validator for unique-email
 * 
 * @author Sanjay Patel
 */
public class UniqueEmailValidator
implements ConstraintValidator<UniqueEmail, String> {

	private static final Log log = LogFactory.getLog(UniqueEmailValidator.class);

	private AbstractUserRepository<?,?> userRepository;

	public UniqueEmailValidator(AbstractUserRepository<?, ?> userRepository) {
		
		this.userRepository = userRepository;
		log.info("Created");
	}

	@Override
	public boolean isValid(String email, ConstraintValidatorContext context) {
		
		log.debug("Validating whether email is unique: " + email);
		return !userRepository.findByEmail(email).isPresent();
	}
}
