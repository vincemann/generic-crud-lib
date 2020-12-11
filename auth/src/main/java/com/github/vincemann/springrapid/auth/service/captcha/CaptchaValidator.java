package com.github.vincemann.springrapid.auth.service.captcha;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.vincemann.aoplog.api.AopLoggable;
import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.springrapid.auth.AuthProperties;
import com.github.vincemann.springrapid.core.slicing.ServiceComponent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;

/**
 * Captcha validation constraint
 * 
 * Reference
 *   http://www.captaindebug.com/2011/07/writng-jsr-303-custom-constraint_26.html#.VIVhqjGUd8E
 *   http://www.captechconsulting.com/blog/jens-alm/versioned-validated-and-secured-rest-services-spring-40-2?_ga=1.71504976.2113127005.1416833905
 * 
 * @author Sanjay Patel
 *
 */
@ServiceComponent
@Slf4j
public class CaptchaValidator implements ConstraintValidator<Captcha, String>, AopLoggable {

	/**
	 * A class to receive the response
	 * from Google 
	 * 
	 * @author Sanjay Patel
	 */
	private static class ResponseData {
		
		private boolean success;
		
		@JsonProperty("error-codes")
		private Collection<String> errorCodes;
		
		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public Collection<String> getErrorCodes() {
			return errorCodes;
		}
		public void setErrorCodes(Collection<String> errorCodes) {
			this.errorCodes = errorCodes;
		}
	}
	
	private AuthProperties properties;
	private RestTemplate restTemplate;
	
	public CaptchaValidator(AuthProperties properties) {
		
		this.properties = properties;
		this.restTemplate = new RestTemplate();

	}


	/**
	 * Does the validation
	 * 
	 * @see <a href="http://www.journaldev.com/7133/how-to-integrate-google-recaptcha-in-java-web-application">Integrating Google Captcha</a>
	 */
	@LogInteraction
	@Override
	public boolean isValid(String captchaResponse, ConstraintValidatorContext context) {		

		// If reCAPTCHA site key is not given as a property,
		// e.g. while testing or getting started,
		// no need to validate.
		if (StringUtils.isBlank(properties.getRecaptcha().getSitekey())) { // 
			log.debug("Captcha validation not done, as it is disabled in application properties.");
			return true;
		}
		
		// Ensure user hasn't submitted a blank captcha response
		if (StringUtils.isBlank(captchaResponse))
	         return false;
	        
		// Prepare the form data for sending to google
		MultiValueMap<String, String> formData =
			new LinkedMultiValueMap<String, String>(2);
		formData.add("response", captchaResponse);
		formData.add("secret", properties.getRecaptcha().getSecretkey());
		
		try {

			// This also works:
			//	ResponseData responseData = restTemplate.postForObject(
			//	   "https://www.google.com/recaptcha/api/siteverify?response={0}&secret={1}",
			//	    null, ResponseData.class, captchaResponse, reCaptchaSecretKey);

			// Post the data to google
			ResponseData responseData = restTemplate.postForObject(
			   "https://www.google.com/recaptcha/api/siteverify",
			   formData, ResponseData.class);

			if (responseData.success) { // Verified by google
				log.debug("Captcha validation succeeded.");
				return true;
			}
			
			log.info("Captcha validation failed.");
			return false;
			
		} catch (Throwable t) {
			log.error(ExceptionUtils.getStackTrace(t));
			return false;
		}
	}
}
