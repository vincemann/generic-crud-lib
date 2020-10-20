package com.github.vincemann.springlemon.auth.service.token;

import com.github.vincemann.aoplog.api.AopLoggable;
import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.springlemon.auth.AuthProperties;
import com.github.vincemann.springlemon.auth.security.JwtClaimsPrincipalConverter;
import com.github.vincemann.springrapid.core.security.RapidAuthenticatedPrincipal;
import com.github.vincemann.springrapid.core.util.Message;
import com.github.vincemann.springrapid.core.util.VerifyAccess;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

@Slf4j
public abstract class AbstractJwtAuthorizationTokenService<P extends RapidAuthenticatedPrincipal>
        implements AuthorizationTokenService<P>, AopLoggable {

    private static final String AUTH_AUDIENCE = "auth";
    private static final String PRINCIPAL_CLAIMS_KEY = "rapid-principal";


    private JwsTokenService jwsTokenService;
    private JwtClaimsPrincipalConverter<P> jwtPrincipalConverter;
    private AuthProperties properties;


    @LogInteraction
    @Override
    public String createToken(P principal) {
        Map<String, Object> principalClaims = jwtPrincipalConverter.toClaims(principal);
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();

        JWTClaimsSet claims = builder
                .expirationTime(new Date(System.currentTimeMillis() + properties.getJwt().getExpirationMillis()))
                .audience(AUTH_AUDIENCE)
                .subject(principal.getName())
                .issueTime(new Date())
                .claim(PRINCIPAL_CLAIMS_KEY,principalClaims)
                .build();

        return jwsTokenService.createToken(claims);
    }

    @LogInteraction
    @Override
    public P parseToken(String token) throws BadTokenException {
        JWTClaimsSet jwtClaimsSet = jwsTokenService.parseToken(token);
        Map<String, Object> principalClaims = (Map<String, Object>) jwtClaimsSet.getClaim(PRINCIPAL_CLAIMS_KEY);
        P principal = jwtPrincipalConverter.toPrincipal(principalClaims);
        verifyToken(jwtClaimsSet,principal);
        return principal;
    }

    public void verifyToken(JWTClaimsSet claims, P principal){
        //expired?
        long expirationTime = claims.getExpirationTime().getTime();
        long currentTime = System.currentTimeMillis();

        log.debug("Parsing JWT. Expiration time = " + expirationTime
                + ". Current time = " + currentTime);

        VerifyAccess.condition(expirationTime >= currentTime,
                Message.get("com.naturalprogrammer.spring.expiredToken"));

        //todo put into EmailVerificationTokenService
//        //not yet valid?
//        //is token still valid?
//        long issueTime = claims.getIssueTime().getTime();
//        LecUtils.ensureCredentials(issueTime >= issuedAfter,
//                "com.naturalprogrammer.spring.obsoleteToken");

    }

    @Autowired
    public void injectJwsTokenService(JwsTokenService jwsTokenService) {
        this.jwsTokenService = jwsTokenService;
    }

    @Autowired
    public void injectJwtPrincipalConverter(JwtClaimsPrincipalConverter<P> jwtPrincipalConverter) {
        this.jwtPrincipalConverter = jwtPrincipalConverter;
    }

    @Autowired
    public void injectProperties(AuthProperties properties) {
        this.properties = properties;
    }
}
