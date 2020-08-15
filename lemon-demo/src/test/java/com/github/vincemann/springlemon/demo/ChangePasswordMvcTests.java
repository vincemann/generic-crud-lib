package com.github.vincemann.springlemon.demo;

import com.github.vincemann.springlemon.auth.domain.dto.ChangePasswordForm;
import com.github.vincemann.springlemon.auth.util.LecUtils;
import com.github.vincemann.springrapid.core.util.MapperUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ChangePasswordMvcTests extends AbstractMvcTests {
	
	private static final String NEW_PASSWORD = "a-new-password";
	
	private ChangePasswordForm changePasswordForm(String oldPassword) {
		
		ChangePasswordForm form = new ChangePasswordForm();
		form.setOldPassword(oldPassword);
		form.setPassword(NEW_PASSWORD);
		form.setRetypePassword(NEW_PASSWORD);
		
		return form;		
	}

	/**
	 * A non-admin user should be able to change his password.
	 */
	@Test
	public void testChangePassword() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/password", UNVERIFIED_USER_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_USER_ID))
				.contentType(MediaType.APPLICATION_JSON)
				.content(MapperUtils.toJson(changePasswordForm(USER_PASSWORD))))
				.andExpect(status().is(204))
				.andExpect(header().string(LecUtils.TOKEN_RESPONSE_HEADER_NAME, containsString(".")));
		
		// Ensure able to login with new password
		login(UNVERIFIED_USER_EMAIL, NEW_PASSWORD);
	}
	
	/**
	 * An good admin user should be able to change the password of another user.
	 */
	@Test
	public void testAdminChangePasswordOfAnotherUser() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/password", UNVERIFIED_USER_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID))
				.contentType(MediaType.APPLICATION_JSON)
				.content(MapperUtils.toJson(changePasswordForm(USER_PASSWORD))))
				.andExpect(status().is(204))
				.andExpect(header().string(LecUtils.TOKEN_RESPONSE_HEADER_NAME, containsString(".")));
		
		// Ensure able to login with new password
		login(UNVERIFIED_USER_EMAIL, NEW_PASSWORD);
	}
	
	/**
	 * Providing an unknown id should return 404.
	 */
	@Test
	public void testChangePasswordOfUnknownId_shouldFail() throws Exception {
		
		mvc.perform(post("/api/core/users/99/password")
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID))
				.contentType(MediaType.APPLICATION_JSON)
				.content(MapperUtils.toJson(changePasswordForm(ADMIN_PASSWORD))))
				.andExpect(status().is(404));
	}
	
	/**
	 * A non-admin user should not be able to change others' password.
	 */
	@Test
	public void testChangePasswordOfAnotherUser_shouldFail() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/password", UNVERIFIED_USER_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(USER_ID))
				.contentType(MediaType.APPLICATION_JSON)
				.content(MapperUtils.toJson(changePasswordForm(USER_PASSWORD))))
				.andExpect(status().is(403));
		
		// Ensure password didn't change
		login(UNVERIFIED_USER_EMAIL, USER_PASSWORD);		
	}

	/**
	 * A  bad admin user should not be able to change others' password.
	 */
	@Test
	public void testBadAdminChangePasswordOfAnotherUser_shouldFail() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/password", UNVERIFIED_USER_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_ADMIN_ID))
				.contentType(MediaType.APPLICATION_JSON)
				.content(MapperUtils.toJson(changePasswordForm(ADMIN_PASSWORD))))
				.andExpect(status().is(403));
		
		// Ensure password didn't change
		login(UNVERIFIED_USER_EMAIL, USER_PASSWORD);		
	}
	
	@Test
	public void testChangePasswordInvalidData() throws Exception {
		
		// All fields null
		mvc.perform(post("/api/core/users/{id}/password", UNVERIFIED_USER_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_USER_ID))
				.contentType(MediaType.APPLICATION_JSON)
				.content(MapperUtils.toJson(new ChangePasswordForm())))
				.andExpect(status().is(422))
				.andExpect(jsonPath("$.errors[*].field").value(hasSize(3)))
				.andExpect(jsonPath("$.errors[*].field").value(hasItems(
						"changePasswordForm.oldPassword",
						 "changePasswordForm.retypePassword",
						 "changePasswordForm.password")));
		
		// All fields too short
		ChangePasswordForm form = new ChangePasswordForm();
		form.setOldPassword("short");
		form.setPassword("short");
		form.setRetypePassword("short");

		mvc.perform(post("/api/core/users/{id}/password", UNVERIFIED_USER_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_USER_ID))
				.contentType(MediaType.APPLICATION_JSON)
				.content(MapperUtils.toJson(form)))
				.andExpect(status().is(422))
				.andExpect(jsonPath("$.errors[*].field").value(hasSize(3)))
				.andExpect(jsonPath("$.errors[*].field").value(hasItems(
						"changePasswordForm.oldPassword",
						 "changePasswordForm.retypePassword",
						 "changePasswordForm.password")));
		
		// different retype-password
		form = changePasswordForm(USER_PASSWORD);
		form.setRetypePassword("different-retype-password");

		mvc.perform(post("/api/core/users/{id}/password", UNVERIFIED_USER_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_USER_ID))
				.contentType(MediaType.APPLICATION_JSON)
				.content(MapperUtils.toJson(form)))
				.andExpect(status().is(422))
				.andExpect(jsonPath("$.errors[*].field").value(hasSize(2)))
				.andExpect(jsonPath("$.errors[*].field").value(hasItems(
						 "changePasswordForm.retypePassword",
						 "changePasswordForm.password")));
	}
}
