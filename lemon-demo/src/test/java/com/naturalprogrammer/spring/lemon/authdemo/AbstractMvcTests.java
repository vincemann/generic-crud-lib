package com.naturalprogrammer.spring.lemon.authdemo;

import com.google.common.collect.Lists;
import com.naturalprogrammer.spring.lemon.authdemo.entities.User;
import com.naturalprogrammer.spring.lemon.authdemo.repositories.UserRepository;
import com.naturalprogrammer.spring.lemon.auth.mail.MailSender;
import com.naturalprogrammer.spring.lemon.auth.service.LemonServiceImpl;
import com.naturalprogrammer.spring.lemon.auth.util.LecUtils;
import io.github.vincemann.springrapid.acl.Role;
import io.github.vincemann.springrapid.acl.service.LocalPermissionService;
import io.github.vincemann.springrapid.acl.service.RunAsUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest({
        "logging.level.com.naturalprogrammer=ERROR", // logging.level.root=ERROR does not work: https://stackoverflow.com/questions/49048298/springboottest-not-overriding-logging-level
        "logging.level.org.springframework=ERROR",
        "lemon.recaptcha.sitekey="
})
@AutoConfigureMockMvc
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.HSQL)
@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql"})
/**
 * Fills tokens Map in an integration test manner by logging all users in
 */
@ActiveProfiles(value = {"web", "service", "test", "webTest", "serviceTest", "dev"}, inheritProfiles = false)
public abstract class AbstractMvcTests {

    protected static final long ADMIN_ID = 101L;
    protected static final long UNVERIFIED_ADMIN_ID = 102L;
    protected static final long BLOCKED_ADMIN_ID = 103L;

    protected static final long USER_ID = 104L;
    protected static final long UNVERIFIED_USER_ID = 105L;
    protected static final long BLOCKED_USER_ID = 106L;

    protected static final String ADMIN_EMAIL = "admin@example.com";
    protected static final String ADMIN_PASSWORD = "admin!";

    protected static final String USER_PASSWORD = "Sanjay99!";
    protected static final String UNVERIFIED_USER_EMAIL = "unverifieduser@example.com";

    protected Map<Long, String> tokens = new HashMap<>(6);

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected LocalPermissionService permissionService;

    @SpyBean
    protected MailSender<?> mailSender;

    @Autowired
    private RunAsUserService runAsUserService;



    protected String login(String userName, String password) throws Exception {

        MvcResult result = mvc.perform(post("/api/core/login")
                .param("username", userName)
                .param("password", password)
                .header("contentType", MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(200))
                .andReturn();

        return result.getResponse().getHeader(LecUtils.TOKEN_RESPONSE_HEADER_NAME);
    }


    @BeforeEach
    public void baseSetUp() throws Exception {
        User admin = userRepository.findById(ADMIN_ID).get();
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(admin.getName(), admin.getPassword()
                , Lists.newArrayList(new SimpleGrantedAuthority(Role.ADMIN)));
        runAsUserService.runAuthenticatedAs(adminAuth,this::saveUsersAclData);

        tokens.put(ADMIN_ID, login(ADMIN_EMAIL, ADMIN_PASSWORD));
        tokens.put(UNVERIFIED_ADMIN_ID, login("unverifiedadmin@example.com", ADMIN_PASSWORD));
        tokens.put(BLOCKED_ADMIN_ID, login("blockedadmin@example.com", ADMIN_PASSWORD));
        tokens.put(USER_ID, login("user@example.com", USER_PASSWORD));
        tokens.put(UNVERIFIED_USER_ID, login(UNVERIFIED_USER_EMAIL, USER_PASSWORD));
        tokens.put(BLOCKED_USER_ID, login("blockeduser@example.com", USER_PASSWORD));
    }

    @Transactional
    public void saveUsersAclData() {
        adminFullAccessOver(USER_ID, UNVERIFIED_USER_ID, BLOCKED_USER_ID, ADMIN_ID, UNVERIFIED_ADMIN_ID, BLOCKED_ADMIN_ID);
        fullAccessAboutSelf(ADMIN_ID, UNVERIFIED_ADMIN_ID, BLOCKED_ADMIN_ID, USER_ID, UNVERIFIED_USER_ID, BLOCKED_USER_ID);
    }

    private void fullAccessAboutSelf(Long... ids) {
        for (Long id : ids) {
            User user = userRepository.findById(id).get();
            permissionService.addPermissionForUserOver(user, BasePermission.ADMINISTRATION, user.getEmail());
        }
    }

    private void adminFullAccessOver(Long... ids) {
        for (Long id : ids) {
            User user = userRepository.findById(id).get();
            permissionService.addPermissionForAuthorityOver(user, BasePermission.ADMINISTRATION, Role.ADMIN);
        }
    }


    protected void ensureTokenWorks(String token) throws Exception {

        mvc.perform(get("/api/core/context")
                .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.user.id").value(UNVERIFIED_USER_ID));
    }
}
