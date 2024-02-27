package com.github.vincemann.springrapid.auth.config;

import com.github.vincemann.springrapid.auth.AuthProperties;
import com.github.vincemann.springrapid.auth.sec.JwtPrincipalConverter;
import com.github.vincemann.springrapid.auth.sec.JwtPrincipalConverterImpl;
import com.github.vincemann.springrapid.auth.service.token.JweServiceImpl;
import com.github.vincemann.springrapid.auth.service.token.JweTokenService;
import com.github.vincemann.springrapid.auth.service.token.JwsServiceImpl;
import com.github.vincemann.springrapid.auth.service.token.JwsTokenService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.KeyLengthException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RapidTokenAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(JwtPrincipalConverter.class)
    public JwtPrincipalConverter jwtClaimsPrincipalConverter(){
        return new JwtPrincipalConverterImpl();
    }

    @Bean
    @ConditionalOnMissingBean(JwsTokenService.class)
    public JwsTokenService jwsTokenService(AuthProperties properties) throws JOSEException {
        return new JwsServiceImpl(properties.getJwt().getSecret());
    }

    @Bean
    @ConditionalOnMissingBean(JweTokenService.class)
    public JweTokenService jweTokenService(AuthProperties properties) throws KeyLengthException {
        return new JweServiceImpl(properties.getJwt().getSecret());
    }
}
