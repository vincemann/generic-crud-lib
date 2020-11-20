package com.github.vincemann.springrapid.authtests;

import static org.hamcrest.Matchers.hasEntry;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.github.vincemann.springrapid.core.security.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

public class BasicAuthTest extends AbstractRapidAuthTest {

//	private static final String TEST_RECAPTCHA = "6LdwxRcUAAAAABkhOGWQXhl9FsR27D5YUJRuGzx0";

	@BeforeEach
	public void setup() throws Exception {
		super.setup();
		Map<String,Object> testSharedProperties = new HashMap<>();
		testSharedProperties.put("testKey","testValue");

		Mockito.when(unproxy(properties).getShared())
				.thenReturn(testSharedProperties);
		// todo reenable captcha
//		Mockito.when(unproxy(properties).getRecaptcha().getSitekey())
//				.thenReturn(TEST_RECAPTCHA);
	}

	@Test
	public void testPing() throws Exception {

		mvc.perform(get(authProperties.getController().getPingUrl()))
				.andExpect(status().is(204));
	}



	@Test
	public void testGetContextLoggedIn() throws Exception {



		mvc.perform(get(authProperties.getController().getContextUrl())
				.header(HttpHeaders.AUTHORIZATION, tokens.get(getAdmin().getId())))
				.andExpect(status().is(200))
//				.andExpect(header().string(HttpHeaders.AUTHORIZATION, containsString(".")))
				// todo reenable captcha
//				.andExpect(jsonPath("$.reCaptchaSiteKey").isString())
				.andExpect(jsonPath("$.shared").value(hasEntry("testKey","testValue")))

				.andExpect(jsonPath("$.user.id").value(getAdmin().getId()))
				.andExpect(jsonPath("$.user.roles[0]").value(Roles.ADMIN))
				.andExpect(jsonPath("$.user.password").doesNotExist())
				.andExpect(jsonPath("$.user.unverified").value(false))
				.andExpect(jsonPath("$.user.blocked").value(false))
				.andExpect(jsonPath("$.user.admin").value(true))
				.andExpect(jsonPath("$.user.goodUser").value(true));
	}
	
	@Test
	public void testGetContextWithoutLoggedIn_shouldOnlyReturnPublicContext() throws Exception {

		mvc.perform(get(authProperties.getController().getContextUrl()))
				.andExpect(status().is(200))
				.andExpect(header().doesNotExist(HttpHeaders.AUTHORIZATION))
				// todo reenable captcha

//				.andExpect(jsonPath("$.reCaptchaSiteKey").isString())
				.andExpect(jsonPath("$.shared").value(hasEntry("testKey","testValue")))
				.andExpect(jsonPath("$.user").doesNotExist());
	}
}
