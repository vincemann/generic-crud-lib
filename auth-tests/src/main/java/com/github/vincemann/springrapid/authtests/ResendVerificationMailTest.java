package com.github.vincemann.springrapid.authtests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.vincemann.springrapid.auth.mail.MailData;
import com.github.vincemann.springrapid.auth.service.AbstractUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.vincemann.springrapid.authtests.adapter.AuthTestAdapter.*;
import static com.github.vincemann.springrapid.core.util.ProxyUtils.aopUnproxy;
public class ResendVerificationMailTest extends RapidAuthIntegrationTest {

	@Test
	public void canResendVerificationMailForOwnAccount() throws Exception {
		String token = login2xx(UNVERIFIED_USER_CONTACT_INFORMATION, UNVERIFIED_USER_PASSWORD);
		mvc.perform(testTemplate.resendVerificationContactInformation(getUnverifiedUser().getContactInformation(),token))
				.andExpect(status().is2xxSuccessful());

		MailData mailData = testTemplate.verifyMailWasSend();
		Assertions.assertEquals(UNVERIFIED_USER_CONTACT_INFORMATION,mailData.getTo());
		Assertions.assertEquals(AbstractUserService.VERIFY_CONTACT_INFORMATION_AUDIENCE,mailData.getTopic());
	}

	@Test
	public void userCanResendVerificationMailOfDiffUser() throws Exception {
		String token = login2xx(USER_CONTACT_INFORMATION, USER_PASSWORD);
		testTemplate.resendVerificationContactInformation2xx(getUnverifiedUser().getContactInformation(),token);
	}

	@Test
	public void adminCanResendVerificationMailOfDiffUser() throws Exception {
		String token = login2xx(ADMIN_CONTACT_INFORMATION, ADMIN_PASSWORD);
		testTemplate.resendVerificationContactInformation2xx(getUnverifiedUser().getContactInformation(),token);
	}

//	@Test
//	public void blockedAdminCantResendVerificationMailOfDiffUser() throws Exception {
//		String token = login2xx(BLOCKED_ADMIN_CONTACT_INFORMATION, BLOCKED_ADMIN_PASSWORD);
//		mvc.perform(testTemplate.resendVerificationContactInformation(getUnverifiedUser().getContactInformation(),token))
//				.andExpect(status().isForbidden());
//
//		verify(unproxy(mailSender), never()).send(any());
//	}

	@Test
	public void anonCantResendVerificationMail() throws Exception {
		mvc.perform(testTemplate.resendVerificationContactInformation(getUnverifiedUser().getContactInformation(),""))
				.andExpect(status().isUnauthorized());
		
		verify(aopUnproxy(mailSender), never()).send(any());
	}
	
	@Test
	public void alreadyVerified_cantResendVerificationMail() throws Exception {
		String token = login2xx(USER_CONTACT_INFORMATION, USER_PASSWORD);
		mvc.perform(testTemplate.resendVerificationContactInformation(getUser().getContactInformation(),token))
				.andExpect(status().isBadRequest());

		verify(aopUnproxy(mailSender), never()).send(any());
	}
	

	
	@Test
	public void cantResendVerificationMailOfNonExistingUser() throws Exception {
		String token = login2xx(USER_CONTACT_INFORMATION, USER_PASSWORD);
		mvc.perform(testTemplate.resendVerificationContactInformation(UNKNOWN_USER_ID,token))
				.andExpect(status().isNotFound());
		
		verify(aopUnproxy(mailSender), never()).send(any());
	}
}
