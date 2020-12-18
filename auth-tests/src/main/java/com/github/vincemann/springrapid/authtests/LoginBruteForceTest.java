package com.github.vincemann.springrapid.authtests;

import com.github.vincemann.springrapid.auth.security.bruteforce.LoginAttemptService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest({
        "lemon.recaptcha.sitekey=",
        "rapid-auth.loginBruteforceProtection=true",
        "rapid-auth.maxLoginAttempts=5"
})
public class LoginBruteForceTest extends AbstractRapidAuthTest{

    static final int MAX_LOGIN_TRIES = 5;

    @Autowired
    LoginAttemptService loginAttemptService;

    @Test
    public void tooManyLoginTries() throws Exception {
        String wrongPassword = "wrongPw";
        for (int i =0 ;i<MAX_LOGIN_TRIES;i++){
            login(ADMIN_EMAIL,wrongPassword+i)
                    .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
        }
        login(ADMIN_EMAIL,wrongPassword)
                .andExpect(status().is(HttpStatus.TOO_MANY_REQUESTS.value()));

    }

    @Test
    public void almostTooManyLoginTries_thenRightLogin_shouldReset() throws Exception {
        String wrongPassword = "wrongPw";
        for (int i =0 ;i<MAX_LOGIN_TRIES-1;i++){
            login(ADMIN_EMAIL,wrongPassword+i)
                    .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
        }
        login(ADMIN_EMAIL,ADMIN_PASSWORD)
                .andExpect(status().is(200));
        for (int i =0 ;i<MAX_LOGIN_TRIES-1;i++){
            login(ADMIN_EMAIL,wrongPassword+i)
                    .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
        }

    }


    @AfterEach
    void resetLimit() throws java.sql.SQLException {
        loginAttemptService.reset();
    }
}
