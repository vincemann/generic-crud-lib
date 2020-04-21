package com.naturalprogrammer.spring.lemon.authdemo;

import com.naturalprogrammer.spring.lemon.authdemo.entities.User;
import com.naturalprogrammer.spring.lemon.auth.security.domain.LemonRole;
import com.naturalprogrammer.spring.lemon.auth.util.LecUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SignupMvcTests extends AbstractMvcTests {
	
	@Test
	public void testSignupWithInvalidData() throws Exception {
		
		User invalidUser = new User("abc", "user1", null);

		mvc.perform(post("/api/core/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(LecUtils.toJson(invalidUser)))
				.andExpect(status().is(422))
				.andExpect(jsonPath("$.errors[*].field").value(hasSize(4)))
				.andExpect(jsonPath("$.errors[*].field").value(hasItems(
					"user.email", "user.password", "user.name")))
				.andExpect(jsonPath("$.errors[*].code").value(hasItems(
						"{com.naturalprogrammer.spring.invalid.email}",
						"{blank.name}",
						"{com.naturalprogrammer.spring.invalid.email.size}",
						"{com.naturalprogrammer.spring.invalid.password.size}")))
				.andExpect(jsonPath("$.errors[*].message").value(hasItems(
						"Not a well formed email address",
						"Name required",
						"Email must be between 4 and 250 characters",
						"Password must be between 6 and 50 characters")));
		
		verify(mailSender, never()).send(any());
	}

	@Test
	public void testSignup() throws Exception {
		
		User user = new User("user.foo@example.com", "user123", "User Foo");

		mvc.perform(post("/api/core/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(LecUtils.toJson(user)))
				.andExpect(status().is(201))
				.andExpect(header().string(LecUtils.TOKEN_RESPONSE_HEADER_NAME, containsString(".")))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.username").value("user.foo@example.com"))
				.andExpect(jsonPath("$.roles").value(hasSize(1)))
				.andExpect(jsonPath("$.roles[0]").value(LemonRole.UNVERIFIED))
//				.andExpect(jsonPath("$.tag.name").value("User Foo"))
				.andExpect(jsonPath("$.unverified").value(true))
				.andExpect(jsonPath("$.blocked").value(false))
				.andExpect(jsonPath("$.admin").value(false))
				.andExpect(jsonPath("$.goodUser").value(false))
				.andExpect(jsonPath("$.goodAdmin").value(false));
				
		verify(mailSender).send(any());

		// Ensure that password got encrypted
		Assertions.assertNotEquals("user123", userRepository.findByEmail("user.foo@example.com").get().getPassword());
	}
	
	@Test
	public void testSignupDuplicateEmail() throws Exception {
		
		User user = new User("user@example.com", "user123", "User");

		mvc.perform(post("/api/core/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(LecUtils.toJson(user)))
				.andExpect(status().is(422));
		
		verify(mailSender, never()).send(any());
	}
}
