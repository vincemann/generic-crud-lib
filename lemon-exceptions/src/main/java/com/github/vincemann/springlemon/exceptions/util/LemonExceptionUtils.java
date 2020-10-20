package com.github.vincemann.springlemon.exceptions.util;

import com.github.vincemann.springlemon.exceptions.ExceptionIdMaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Useful helper methods
 * 
 * @author Sanjay Patel
 */
@Slf4j
public class LemonExceptionUtils {
	


	private static LocalValidatorFactoryBean validator;
	private static ExceptionIdMaker exceptionIdMaker;
	
//	public static final MultiErrorException NOT_FOUND_EXCEPTION = new MultiErrorException();

	/**
	 * Constructor
	 */
	public LemonExceptionUtils(LocalValidatorFactoryBean validator,
							   ExceptionIdMaker exceptionIdMaker) {
		
		LemonExceptionUtils.validator=validator;
		LemonExceptionUtils.exceptionIdMaker=exceptionIdMaker;
	}

	public static String getExceptionId(Throwable ex) {

		Throwable root = getRootException(ex);
		return exceptionIdMaker.make(root);
	}


	private static Throwable getRootException(Throwable ex) {

		if (ex == null) return null;

		while(ex.getCause() != null)
			ex = ex.getCause();

		return ex;
	}


	public static LocalValidatorFactoryBean validator() {
		return validator;
	}

	
//	@PostConstruct
//	public void postConstruct() {
//
//		NOT_FOUND_EXCEPTION
//			.httpStatus(HttpStatus.NOT_FOUND)
//			.validate(false, "com.naturalprogrammer.spring.notFound");
//
//		log.info("NOT_FOUND_EXCEPTION built");
//	}

	



	//todo replace with BadEntityException maybe?




	
//	/**
//	 * Throws 404 Error is the entity isn't found
//	 */
//	public static <T> void ensureFound(T entity) {
//
//		LexUtils.validate(entity != null,
//			"com.naturalprogrammer.spring.notFound")
//			.httpStatus(HttpStatus.NOT_FOUND).go();
//	}

//	/**
//	 * Throws 404 Error is the entity isn't found
//	 */
//	public static <T> void ensureFound(Optional<T> entity) {
//
//		LexUtils.validate(entity.isPresent(),
//				"com.naturalprogrammer.spring.notFound")
//				.httpStatus(HttpStatus.NOT_FOUND).go();
//	}

	
//	/**
//	 * Supplys a 404 exception
//	 */
//	public static Supplier<MultiErrorException> notFoundSupplier() {
//
//		return () -> NOT_FOUND_EXCEPTION;
//	}

}
