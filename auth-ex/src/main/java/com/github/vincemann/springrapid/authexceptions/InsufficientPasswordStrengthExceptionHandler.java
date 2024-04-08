package com.github.vincemann.springrapid.authexceptions;

import com.github.vincemann.springrapid.auth.ErrorCodes;
import com.github.vincemann.springrapid.auth.val.InsufficientPasswordStrengthException;
import com.github.vincemann.springrapid.exceptionsapi.AbstractExceptionHandler;
import org.springframework.http.HttpStatus;

public class InsufficientPasswordStrengthExceptionHandler extends AbstractExceptionHandler<InsufficientPasswordStrengthException> {

    public InsufficientPasswordStrengthExceptionHandler() {
        super(InsufficientPasswordStrengthException.class);
    }

    @Override
    public HttpStatus getStatus(InsufficientPasswordStrengthException ex) {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getExceptionId(InsufficientPasswordStrengthException ex) {
        return String.valueOf(ErrorCodes.INSUFFICIENT_PASSWORD_STRENGTH);
    }

}