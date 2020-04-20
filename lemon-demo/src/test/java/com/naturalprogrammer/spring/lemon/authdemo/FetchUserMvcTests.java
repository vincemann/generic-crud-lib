package com.naturalprogrammer.spring.lemon.authdemo;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql"})
public class FetchUserMvcTests extends AbstractMvcTests {
	
	@Test
	public void testUnauthenticated_fetchUserById_shouldOnlyHaveNamePayLoad() throws Exception {
		
		mvc.perform(get("/api/core/users/{id}", ADMIN_ID))
                .andExpect(status().is(200))
				.andExpect(jsonPath("$.id").value(ADMIN_ID))
				.andExpect(jsonPath("$.email").doesNotExist())
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.credentialsUpdatedAt").doesNotExist());
//				.andExpect(jsonPath("$.name").value("Admin 1"));
	}

	@Test
	public void testFetchUserByIdLoggedIn() throws Exception {
		//can see email = can see everything relevant
		// Same user logged in -> can also see its email but that's it
		mvc.perform(get("/api/core/users/{id}", ADMIN_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID)))
                .andExpect(status().is(200))
				.andExpect(jsonPath("$.id").value(ADMIN_ID))
				.andExpect(jsonPath("$.email").value(ADMIN_EMAIL))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.credentialsUpdatedAt").doesNotExist());
//				.andExpect(jsonPath("$.name").value("Admin 1"));
		
		// Another user logged in, can see the same as unauthenticated
		mvc.perform(get("/api/core/users/{id}", ADMIN_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_USER_ID)))
                .andExpect(status().is(200))
				.andExpect(jsonPath("$.id").value(ADMIN_ID))
				.andExpect(jsonPath("$.email").doesNotExist());

		// Admin user logged in - fetching another user can see email
		MvcResult mvcResult = mvc.perform(get("/api/core/users/{id}", UNVERIFIED_USER_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID)))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.id").value(UNVERIFIED_USER_ID))
				.andExpect(jsonPath("$.email").value(UNVERIFIED_USER_EMAIL))
				.andReturn();

	}
	
	@Test
	public void testFetchNonExistingUserById_shouldNotFound() throws Exception {
		
		mvc.perform(get("/api/core/users/99"))
                .andExpect(status().is(404));
	}

	@Test
	public void testUnauthenticated_fetchUserByEmail() throws Exception {
		
		mvc.perform(post("/api/core/users/fetch-by-email")
                .param("email", ADMIN_EMAIL)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(200))
				.andExpect(jsonPath("$.id").value(ADMIN_ID))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.credentialsUpdatedAt").doesNotExist());
//				.andExpect(jsonPath("$.name").value("Admin 1"));
	}

	@Test
	public void testFetchUserByInvalidEmail() throws Exception {
		
		// email does not exist
		mvc.perform(post("/api/core/users/fetch-by-email")
                .param("email", "foo@example.com")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(404));

		// Blank email
		mvc.perform(post("/api/core/users/fetch-by-email")
                .param("email", "")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(422));

		// Invalid email
		mvc.perform(post("/api/core/users/fetch-by-email")
                .param("email", "invalid-email")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(422));
	}
}
