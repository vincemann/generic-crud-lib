package com.naturalprogrammer.spring.lemon.authdemo;

import com.naturalprogrammer.spring.lemon.authdemo.entities.User;
import com.naturalprogrammer.spring.lemon.auth.security.domain.LemonRole;
import com.naturalprogrammer.spring.lemon.auth.util.LecUtils;
import io.github.vincemann.springrapid.acl.Role;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql"})
public class UpdateUserMvcTests extends AbstractMvcTests {
	
	private static final String UPDATED_NAME = "Edited name";
	
    private String userPatch;
    private String userPatchAdminRole;
    private String userPatchNullName;
    private String userPatchLongName;
	
	@Value("classpath:/update-user/patch-update-user.json")
	public void setUserPatch(Resource patch) throws IOException {
		this.userPatch = LecUtils.toStr(patch);
	}
	
	@Value("classpath:/update-user/patch-admin-role.json")
	public void setUserPatchAdminRole(Resource patch) throws IOException {
		this.userPatchAdminRole = LecUtils.toStr(patch);;
	}

	@Value("classpath:/update-user/patch-null-name.json")
	public void setUserPatchNullName(Resource patch) throws IOException {
		this.userPatchNullName = LecUtils.toStr(patch);;
	}

	@Value("classpath:/update-user/patch-long-name.json")
	public void setUserPatchLongName(Resource patch) throws IOException {
		this.userPatchLongName = LecUtils.toStr(patch);;
	}

	/**
	 * A non-admin user should be able to update his own name,
	 * but changes in roles should be skipped.
	 * The name of security principal object should also
	 * change in the process.
	 * @throws Exception 
	 */
	@Test
    public void testUpdateSelf() throws Exception {
		
		mvc.perform(patch("/api/core/users/{id}", UNVERIFIED_USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_USER_ID))
				.content(userPatch))
				.andExpect(status().is(200))
				.andExpect(header().string(LecUtils.TOKEN_RESPONSE_HEADER_NAME, containsString(".")))
//				.andExpect(jsonPath("$.tag.name").value(UPDATED_NAME))
				.andExpect(jsonPath("$.roles").value(hasSize(1)))
				.andExpect(jsonPath("$.roles[0]").value(LemonRole.UNVERIFIED))
				.andExpect(jsonPath("$.username").value(UNVERIFIED_USER_EMAIL));
		
		User user = userRepository.findById(UNVERIFIED_USER_ID).get();
		
		// Ensure that data changed properly
		Assertions.assertEquals(UNVERIFIED_USER_EMAIL, user.getEmail());
		Assertions.assertEquals(1, user.getRoles().size());
		Assertions.assertTrue(user.getRoles().contains(LemonRole.UNVERIFIED));
//		//todo hier war vorher 2 expected... soll nach nem update nen automatischer version increment stattfinden? finde ich nirgends im code
//		Assertions.assertEquals(2L, user.getVersion().longValue());
//
//		// Version mismatch
//		mvc.perform(patch("/api/core/users/{id}", UNVERIFIED_USER_ID)
//				.contentType(MediaType.APPLICATION_JSON)
//				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_USER_ID))
//				.content(userPatch))
//				.andExpect(status().is(409));
    }

	/**
	 * A good ADMIN should be able to update another user's name and roles.
	 * The name of security principal object should NOT change in the process,
	 * and the verification code should get set/unset on addition/deletion of
	 * the UNVERIFIED role. 
	 * @throws Exception 
	 */
	@Test
    public void testGoodAdminCanUpdateOther() throws Exception {
		
		mvc.perform(patch("/api/core/users/{id}", UNVERIFIED_USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID))
				.content(userPatch))
				.andExpect(status().is(200))
				.andExpect(header().string(LecUtils.TOKEN_RESPONSE_HEADER_NAME, containsString(".")))
				.andExpect(jsonPath("$.id").value(UNVERIFIED_USER_ID))
//				.andExpect(jsonPath("$.tag.name").value(UPDATED_NAME))
				.andExpect(jsonPath("$.roles").value(hasSize(1)))
				.andExpect(jsonPath("$.roles[0]").value(Role.ADMIN))
				.andExpect(jsonPath("$.username").value(UNVERIFIED_USER_EMAIL));
		
		User user = userRepository.findById(UNVERIFIED_USER_ID).get();
    	
		// Ensure that data changed properly
		Assertions.assertEquals(UNVERIFIED_USER_EMAIL, user.getEmail());
		Assertions.assertEquals(1, user.getRoles().size());
		Assertions.assertTrue(user.getRoles().contains(Role.ADMIN));
    }
	
	/**
	 * Providing an unknown id should return 404.
	 */
	@Test
    public void testUpdateUnknownId() throws Exception {
    	
		mvc.perform(patch("/api/core/users/{id}", 99)
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID))
				.content(userPatch))
				.andExpect(status().is(404));
    }
	
	/**
	 * A non-admin trying to update the name and roles of another user should throw exception
	 * @throws Exception 
	 */
	@Test
    public void testUpdateAnotherUser() throws Exception {
    	
		mvc.perform(patch("/api/core/users/{id}", ADMIN_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_USER_ID))
				.content(userPatch))
				.andExpect(status().is(403));
    }

	/**
	 * A bad ADMIN trying to update the name and roles of another user should throw exception
	 * @throws Exception 
	 */
	@Test
    public void testBadAdminUpdateAnotherUser() throws Exception {
		
		mvc.perform(patch("/api/core/users/{id}", UNVERIFIED_USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_ADMIN_ID))
				.content(userPatch))
				.andExpect(status().is(403));

		mvc.perform(patch("/api/core/users/{id}", UNVERIFIED_USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(BLOCKED_ADMIN_ID))
				.content(userPatch))
				.andExpect(status().is(403));
	}

	/**
	 * A good ADMIN should not be able to change his own roles
	 * @throws Exception 
	 */
	@Test
    public void goodAdminCanNotUpdateSelfRoles() throws Exception {
    	
		mvc.perform(patch("/api/core/users/{id}", ADMIN_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID))
				.content(userPatchAdminRole))
				.andExpect(status().is(200))
//				.andExpect(jsonPath("$.tag.name").value(UPDATED_NAME))
				.andExpect(jsonPath("$.roles").value(hasSize(1)))
				.andExpect(jsonPath("$.roles[0]").value(Role.ADMIN));
    }
	
//	/**
//	 * Invalid name
//	 * @throws Exception
//	 */
//	@Test
//    public void testUpdateUserInvalidNewName() throws Exception {
//
//		// Null name
//		mvc.perform(patch("/user/update", UNVERIFIED_USER_ID)
//				.contentType(MediaType.APPLICATION_JSON)
//				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_USER_ID))
//				.content(userPatchNullName))
//				.andExpect(status().is(422));
//
//		// Too long name
//		mvc.perform(patch("/api/core/users/{id}", UNVERIFIED_USER_ID)
//				.contentType(MediaType.APPLICATION_JSON)
//				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_USER_ID))
//				.content(userPatchLongName))
//				.andExpect(status().is(422));
//    }
}
