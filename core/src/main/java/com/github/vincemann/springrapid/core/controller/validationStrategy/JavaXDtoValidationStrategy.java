package com.github.vincemann.springrapid.core.controller.validationStrategy;

import javax.validation.*;
import java.util.Set;

/**
 * BaseImpl of {@link DtoValidationStrategy}, that utilizes the javax validation API.
 * See: {@link Validator}
 * @param <Id>
 */
public class JavaXDtoValidationStrategy implements DtoValidationStrategy {
    private final Validator validator;

    public JavaXDtoValidationStrategy(Validator validator) {
//        ValidatorFactory factory=Validation.buildDefaultValidatorFactory();
//        this.validator=factory.getValidator();
        this.validator = validator;
    }

    @Override
    public void validate(Object dto) throws ConstraintViolationException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(dto);
        if(!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);
    }

}
