package com.github.vincemann.springrapid.core.controller.validationStrategy;

import com.github.vincemann.aoplog.api.AopLoggable;
import com.github.vincemann.aoplog.api.LogInteraction;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.io.Serializable;


public interface DtoValidationStrategy extends AopLoggable {

    /**
     * checks whether the Dto entity, read from the {@link HttpServletRequest} is valid
     * @param dto           Dto Entity read from the {@link HttpServletRequest}
     * @throws ConstraintViolationException     is thrown, when Dto Entity {@param dto} is not valid
     */
    @LogInteraction
    public abstract void validate(Object dto) throws ConstraintViolationException;


}
