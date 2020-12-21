package com.github.vincemann.springlemon.exceptions.config;

import java.util.List;

import com.github.vincemann.springlemon.exceptions.ErrorResponseFactory;
import com.github.vincemann.springlemon.exceptions.LemonErrorResponseFactory;
import com.github.vincemann.springrapid.exceptionsapi.AbstractExceptionHandler;
import com.github.vincemann.springlemon.exceptions.web.LemonExceptionHandlerControllerAdvice;
import com.github.vincemann.springlemon.exceptions.web.LemonErrorAttributes;
import com.github.vincemann.springlemon.exceptions.web.LemonErrorController;
import com.github.vincemann.springrapid.core.slicing.WebConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;

@WebConfig
//@AutoConfigureBefore({ValidationAutoConfiguration.class})
@Slf4j
//@ComponentScan(basePackageClasses=AbstractExceptionHandler.class) cant override anymore in this config
public class LemonWebExceptionsAutoConfiguration {


	public LemonWebExceptionsAutoConfiguration() {

	}


	/**
	 * Configures ErrorResponseComposer if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(LemonErrorResponseFactory.class)
	public <T extends Throwable>
	ErrorResponseFactory<T> errorResponseComposer(List<AbstractExceptionHandler<T>> handlers) {
		
		return new LemonErrorResponseFactory<T>(handlers);
	}


	/**
	 * Configures DefaultExceptionHandlerControllerAdvice if missing
	 */
	@Bean
	@ConditionalOnMissingBean(LemonExceptionHandlerControllerAdvice.class)
	public <T extends Throwable>
	LemonExceptionHandlerControllerAdvice<T> lemonExceptionHandlerControllerAdvice(
			LemonErrorResponseFactory<T> errorResponseFactory) {

		return new LemonExceptionHandlerControllerAdvice<T>(errorResponseFactory);
	}

	/**
	 * Configures an Error Attributes if missing
	 */
	@Bean
	@ConditionalOnMissingBean(ErrorAttributes.class)
	public <T extends Throwable>
	ErrorAttributes errorAttributes(LemonErrorResponseFactory<T> errorResponseFactory) {
		return new LemonErrorAttributes<T>(errorResponseFactory);
	}

	/**
	 * Configures an Error Controller if missing
	 */
	@Bean
	@ConditionalOnMissingBean(ErrorController.class)
	public ErrorController errorController(ErrorAttributes errorAttributes,
										   ServerProperties serverProperties,
										   List<ErrorViewResolver> errorViewResolvers) {

		return new LemonErrorController(errorAttributes, serverProperties, errorViewResolvers);
	}

}
