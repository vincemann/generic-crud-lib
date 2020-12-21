package com.github.vincemann.springrapid.core.handler;

import com.github.vincemann.springrapid.exceptionsapi.AbstractBadRequestExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.fasterxml.jackson.core.JsonParseException;

//@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class JsonParseExceptionHandler extends AbstractBadRequestExceptionHandler<JsonParseException> {

	public JsonParseExceptionHandler() {
		
		super(JsonParseException.class);

	}
}
