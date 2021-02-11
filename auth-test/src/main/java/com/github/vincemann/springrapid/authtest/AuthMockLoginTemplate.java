package com.github.vincemann.springrapid.authtest;

import com.github.vincemann.springrapid.auth.domain.AbstractUser;
import com.github.vincemann.springrapid.auth.security.AuthenticatedPrincipalFactory;
import com.github.vincemann.springrapid.coretest.login.MockLoginTemplate;
import com.github.vincemann.springrapid.coretest.login.RapidMockLoginTemplate;
import org.springframework.beans.factory.annotation.Autowired;


public class AuthMockLoginTemplate implements MockLoginTemplate<AbstractUser> {

    private AuthenticatedPrincipalFactory authenticatedPrincipalFactory;
    private RapidMockLoginTemplate mockLoginTemplate;

    @Override
    public void login(AbstractUser principal) {
        mockLoginTemplate.login(authenticatedPrincipalFactory.create(principal));
    }

    @Autowired
    public void injectAuthenticatedPrincipalFactory(AuthenticatedPrincipalFactory authenticatedPrincipalFactory) {
        this.authenticatedPrincipalFactory = authenticatedPrincipalFactory;
    }
    @Autowired
    public void injectMockAuthLoginTemplate(RapidMockLoginTemplate mockLoginTemplate) {
        this.mockLoginTemplate = mockLoginTemplate;
    }
}
