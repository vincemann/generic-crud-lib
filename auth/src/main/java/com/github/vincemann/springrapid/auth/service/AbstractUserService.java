package com.github.vincemann.springrapid.auth.service;


import com.github.vincemann.springrapid.auth.AuthProperties;
import com.github.vincemann.springrapid.auth.domain.AbstractUser;
import com.github.vincemann.springrapid.auth.domain.AbstractUserRepository;
import com.github.vincemann.springrapid.auth.domain.AuthRoles;
import com.github.vincemann.springrapid.auth.domain.RapidAuthAuthenticatedPrincipal;
import com.github.vincemann.springrapid.auth.domain.dto.ChangePasswordForm;
import com.github.vincemann.springrapid.auth.domain.dto.RequestEmailChangeForm;
import com.github.vincemann.springrapid.auth.domain.dto.ResetPasswordForm;
import com.github.vincemann.springrapid.auth.mail.MailData;
import com.github.vincemann.springrapid.auth.mail.MailSender;
import com.github.vincemann.springrapid.auth.security.AuthenticatedPrincipalFactory;
import com.github.vincemann.springrapid.auth.service.token.AuthorizationTokenService;
import com.github.vincemann.springrapid.auth.service.token.BadTokenException;
import com.github.vincemann.springrapid.auth.service.token.JweTokenService;
import com.github.vincemann.springrapid.auth.util.LemonMapUtils;
import com.github.vincemann.springrapid.auth.util.RapidJwt;
import com.github.vincemann.springrapid.auth.util.TransactionalUtils;
import com.github.vincemann.springrapid.core.security.RapidSecurityContext;
import com.github.vincemann.springrapid.core.service.JPACrudService;
import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import com.github.vincemann.springrapid.core.service.password.RapidPasswordEncoder;
import com.github.vincemann.springrapid.core.util.Message;
import com.github.vincemann.springrapid.core.util.VerifyAccess;
import com.github.vincemann.springrapid.core.util.VerifyEntity;
import com.google.common.collect.Sets;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.ValidationException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Validated
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
@Slf4j
public abstract class AbstractUserService
        <
                U extends AbstractUser<ID>,
                ID extends Serializable,
                R extends AbstractUserRepository<U, ID>
                >
        extends JPACrudService<U, ID, R>
        implements UserService<U, ID> {

    public static final String CHANGE_EMAIL_AUDIENCE = "change-email";
    public static final String VERIFY_AUDIENCE = "verify";
    public static final String FORGOT_PASSWORD_AUDIENCE = "forgot-password";

    private AuthorizationTokenService<RapidAuthAuthenticatedPrincipal> authorizationTokenService;
    private RapidSecurityContext<RapidAuthAuthenticatedPrincipal> securityContext;
    private AuthenticatedPrincipalFactory authenticatedPrincipalFactory;
    private RapidPasswordEncoder passwordEncoder;
    private AuthProperties properties;
    private MailSender<MailData> mailSender;
    private JweTokenService jweTokenService;
//    private UserService<U, ID> rootUserService;

    /**
     * Creates a new user object. Must be overridden in the
     * subclass, like this:
     *
     * <pre>
     * public User newUser() {
     *    return new User();
     * }
     * </pre>
     */
    public abstract U newUser();


    @Override
    public Map<String, Object> getContext() {

        // make the context
        Map<String, Object> context = new HashMap<String, Object>(3);
        context.put("reCaptchaSiteKey", properties.getRecaptcha().getSitekey());
        context.put("shared", properties.getShared());
        RapidAuthAuthenticatedPrincipal principal = securityContext.currentPrincipal();
        if (principal != null) {
            if (!principal.isAnon()) {
                RapidAuthAuthenticatedPrincipal withoutPw = new RapidAuthAuthenticatedPrincipal(principal);
                withoutPw.setPassword(null);
                context.put("user", withoutPw);
            }
        }

        return context;
    }

    protected void checkValidPassword(String password) throws BadEntityException {
        //must be at least 8 chars long and contain a number
        if(password==null){
            throw new BadEntityException("Must not be null");
        }
        if(password.length()<8){
            throw new BadEntityException("At least 8 chars expected");
        }
        if(!password.matches(".*\\d.*")){
            throw new BadEntityException("Must contain at least one number");
        }
    }

    protected void checkUniqueEmail(String email) throws AlreadyRegisteredException {
        Optional<U> byEmail = getRepository().findByEmail(email);
        if (byEmail.isPresent()){
            throw new AlreadyRegisteredException("Email: " + email + " is already taken");
        }
    }

    /**
     * Signs up a user.
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public U signup(U user) throws BadEntityException, AlreadyRegisteredException {
        //admins get created with createAdminMethod
        user.setRoles(Sets.newHashSet(AuthRoles.USER));
        checkValidPassword(user.getPassword());
        checkUniqueEmail(user.getEmail());
        U saved = save(user);
        // is done in same transaction -> so applied directly
        makeUnverified(saved);

        log.debug("saved and send verification mail for unverified new user: " + saved);
        //logout anon and login user, it is expected that signed up user is logged in after this method is called
        securityContext.login(authenticatedPrincipalFactory.create(saved));
        return saved;
    }


    @Transactional(propagation = Propagation.SUPPORTS, readOnly = false)
    @Override
    public U save(U user) throws BadEntityException {
        // no restrictions in save method, all restrictions and checks in more abstract methods such as signup and createAdmin
        encodePassword(user);
        return super.save(user);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = false)
    @Override
    public void deleteById(ID id) throws EntityNotFoundException, BadEntityException {
        super.deleteById(id);
    }

    /**
     * Makes a user unverified
     */
    protected void makeUnverified(U user) {
        user.getRoles().add(AuthRoles.UNVERIFIED);
        user.setCredentialsUpdatedMillis(System.currentTimeMillis());
        TransactionalUtils.afterCommit(() -> sendVerificationMail(user));
    }

    /**
     * Only encrypts password, if it is not already encrypted.
     */
    protected void encodePassword(U user){
        String password = user.getPassword();
        if (password==null){
            return;
        }
        if (!passwordEncoder.isEncrypted(password)){
            user.setPassword(passwordEncoder.encode(password));
        }
    }

    /**
     * Resends verification mail to the user.
     */
    public void resendVerificationMail(U user) throws EntityNotFoundException, BadEntityException {

//        // The user must exist

        VerifyEntity.isPresent(user, "User not found");
        // must be unverified
        VerifyEntity.is(user.getRoles().contains(AuthRoles.UNVERIFIED)," Already verified");

        sendVerificationMail(user);
    }


    /**
     * Fetches a user by email
     */
    public Optional<U> findByEmail(String email) {
        return getRepository().findByEmail(email);
    }


    /**
     * Verifies the email id of current-user
     *
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public U verifyUser(U user, String verificationCode) throws EntityNotFoundException, BadEntityException {
        try {
            VerifyEntity.isPresent(user, "User not found");
            // ensure that he is unverified
            // this makes sense to do here not in security plugin
            VerifyEntity.is(user.hasRole(AuthRoles.UNVERIFIED),"Already Verified");
            //verificationCode is jwtToken
            JWTClaimsSet claims = jweTokenService.parseToken(verificationCode);
            RapidJwt.validate(claims,VERIFY_AUDIENCE,user.getCredentialsUpdatedMillis());

            VerifyAccess.condition(claims.getSubject().equals(user.getId().toString()) &&
                            claims.getClaim("email").equals(user.getEmail()),
                    Message.get("com.naturalprogrammer.spring.wrong.verificationCode"));


            //no login needed bc token of user is appended in controller -> we avoid dynamic logins in a stateless env
            //also to be able to use read-only security test -> generic principal type does not need to be passed into this class
            return verifyUser(user);
        }catch (BadTokenException e){
            throw new BadEntityException(e);
        }

    }

    protected U verifyUser(U user) throws BadEntityException, EntityNotFoundException {
        user.getRoles().remove(AuthRoles.UNVERIFIED);
        user.setCredentialsUpdatedMillis(System.currentTimeMillis());
        U saved = update(user);
        log.debug("Verified user: " + saved);
        return saved;
    }


    /**
     * Forgot password.
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void forgotPassword(String email) throws EntityNotFoundException {
        // fetch the user record from database
        Optional<U> byEmail = findByEmail(email);
        VerifyEntity.isPresent(byEmail, "User with email: " + email + " not found");
        U user = byEmail.get();
        sendForgotPasswordMail(user);
    }


    /**
     * Resets the password.
     *
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public U resetPassword(ResetPasswordForm form) throws EntityNotFoundException,  BadEntityException {

        try {
            JWTClaimsSet claims = jweTokenService.parseToken(form.getCode());
            RapidJwt.validate(claims,FORGOT_PASSWORD_AUDIENCE);

            checkValidPassword(form.getNewPassword());

            String email = claims.getSubject();

            // fetch the user
            Optional<U> byEmail = findByEmail(email);
            VerifyEntity.isPresent(byEmail, "User with email: " + email + " not found");
            U user = byEmail.get();
            RapidJwt.validateIssuedAfter(claims, user.getCredentialsUpdatedMillis());

            // sets the password
            user.setPassword(passwordEncoder.encode(form.getNewPassword()));
            user.setCredentialsUpdatedMillis(System.currentTimeMillis());
            //user.setForgotPasswordCode(null);
            try {
                return update(user);
            }catch (BadEntityException e) {
                throw new RuntimeException("Could not reset users password", e);
            }
        } catch (BadTokenException e){
            throw new BadEntityException(e);
        }
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public U update(U update, Boolean full) throws EntityNotFoundException,  BadEntityException {
        Optional<U> old = findById(update.getId());
        VerifyEntity.isPresent(old, "Entity to update with id: " + update.getId() + " not found");
        //update roles works in transaction -> changes are applied on the fly
        updateRoles(old.get(), update);
        update.setRoles(old.get().getRoles());
        String password = update.getPassword();
        if (password!=null) {
            if (!getPasswordEncoder().isEncrypted(password)) {
                checkValidPassword(password);
            }
        }
        encodePassword(update);
        return super.update(update, full);
    }

    /**
     * Changes the password.
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void changePassword(U user, ChangePasswordForm changePasswordForm) throws EntityNotFoundException, BadEntityException {
        VerifyEntity.isPresent(user, "User not found");
        String oldPassword = user.getPassword();

        if (!changePasswordForm.getPassword().equals(changePasswordForm.getRetypePassword())){
            throw new BadEntityException("Password does not match retype password");
        }
        checkValidPassword(changePasswordForm.getPassword());
        // checks
        VerifyEntity.is(
                passwordEncoder.matches(changePasswordForm.getOldPassword(),
                        oldPassword),"Wrong password");

        // sets the password
        user.setPassword(passwordEncoder.encode(changePasswordForm.getPassword()));
        user.setCredentialsUpdatedMillis(System.currentTimeMillis());
        try {
            update(user);
        } catch (BadEntityException e) {
            throw new RuntimeException("Could not change users password", e);
        }

    }


    protected void updateRoles(U old, U newUser) {
        log.debug("Updating user fields for user: " + old);
        // update the roles

        if (old.getRoles().equals(newUser.getRoles())) // roles are same
            return;

        if (newUser.hasRole(AuthRoles.UNVERIFIED)) {

            if (!old.hasRole(AuthRoles.UNVERIFIED)) {
                makeUnverified(old); // make user unverified
            }
        } else {

            if (old.hasRole(AuthRoles.UNVERIFIED))
                old.getRoles().remove(AuthRoles.UNVERIFIED); // make user verified
        }

        old.setRoles(newUser.getRoles());
        old.setCredentialsUpdatedMillis(System.currentTimeMillis());
    }


    /**
     * Requests for email change.
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void requestEmailChange(U user,  RequestEmailChangeForm emailChangeForm) throws EntityNotFoundException, AlreadyRegisteredException {
//        log.debug("Requesting email change for user" + user);
        // checks
//        Optional<U> byId = getUserService().findById(userId);
//        LexUtils.ensureFound(byId);
//        U user = byId.get();
        VerifyEntity.isPresent(user, "User not found");
        checkUniqueEmail(emailChangeForm.getNewEmail());

//        LexUtils.validateField("updatedUser.password",
//                passwordEncoder.matches(emailChangeForm.getPassword(),
//                        user.getPassword()),
//                "com.naturalprogrammer.spring.wrong.password").go();

        // preserves the new email id
        user.setNewEmail(emailChangeForm.getNewEmail());
        //user.setChangeEmailCode(LemonValidationUtils.uid());
        try {
            U saved = update(user);
            // after successful commit, mails a link to the user
            TransactionalUtils.afterCommit(() -> mailChangeEmailLink(saved));
        } catch (BadEntityException e) {
            throw new RuntimeException("Email was malformed, although validation check was successful");
        }

        log.debug("Requested email change: " + user);
    }


    /**
     * Mails the change-email verification link to the user.
     */
    protected void mailChangeEmailLink(U user) {
        JWTClaimsSet claims = RapidJwt.create(CHANGE_EMAIL_AUDIENCE,
                user.getId().toString(),
                properties.getJwt().getExpirationMillis(),
                LemonMapUtils.mapOf("newEmail", user.getNewEmail()));
        String changeEmailCode = jweTokenService.createToken(claims);

        try {

            log.debug("Mailing change email link to user: " + user);

            // make the link
            String changeEmailLink = properties.getApplicationUrl()
                    + "/users/" + user.getId()
                    + "/change-email?code=" + changeEmailCode;

            // mail it
            mailChangeEmailLink(user, changeEmailLink);

            log.debug("Change email link mail queued.");

        } catch (Throwable e) {
            // In case of exception, just log the error and keep silent
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }


    /**
     * Mails the change-email verification link to the user.
     * <p>
     * Override this method if you're using a different MailData
     */
    protected void mailChangeEmailLink(U user, String changeEmailLink) {
        mailSender.send(MailData.of(user.getNewEmail(),
                Message.get(
                        "com.naturalprogrammer.spring.changeEmailSubject"),
                Message.get(
                        "com.naturalprogrammer.spring.changeEmailEmail",
                        changeEmailLink)));
    }


    /**
     * Change the email.
     *
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public U changeEmail(U user, String changeEmailCode) throws EntityNotFoundException, BadEntityException {

        VerifyEntity.isPresent(user, "User not found");
        VerifyEntity.is(StringUtils.isNotBlank(user.getNewEmail()),"No new email found. Looks like you have already changed.");

        JWTClaimsSet claims;
        try {
            claims = jweTokenService.parseToken(changeEmailCode);
        }catch (BadTokenException e){
            throw new BadEntityException(e);
        }
        RapidJwt.validate(claims,CHANGE_EMAIL_AUDIENCE, user.getCredentialsUpdatedMillis());

        VerifyAccess.condition(
                claims.getSubject().equals(user.getId().toString()) &&
                        claims.getClaim("newEmail").equals(user.getNewEmail()),
                Message.get("com.naturalprogrammer.spring.wrong.changeEmailCode"));

        // Ensure that the email would be unique
        VerifyEntity.is(
                !findByEmail(user.getNewEmail()).isPresent(),"Email Id already used");

        // update the fields
        user.setEmail(user.getNewEmail());
        user.setNewEmail(null);
        //user.setChangeEmailCode(null);
        user.setCredentialsUpdatedMillis(System.currentTimeMillis());

        // make the user verified if he is not
        if (user.hasRole(AuthRoles.UNVERIFIED))
            user.getRoles().remove(AuthRoles.UNVERIFIED);

        try {
            return update(user);
        } catch (BadEntityException e) {
            throw new RuntimeException("Could not update users email", e);
        }
    }


    /**
     * Fetches a new token
     *
     * @return
     */
    @Override
    public String createNewAuthToken(String targetUserEmail) {
        return authorizationTokenService.createToken(securityContext.currentPrincipal());
    }

    @Override
    public String createNewAuthToken() {
        return createNewAuthToken(securityContext.currentPrincipal().getEmail());
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    //only called internally
    public U createAdminUser(AuthProperties.Admin admin) throws BadEntityException, AlreadyRegisteredException {
        checkUniqueEmail(admin.getEmail());
        checkValidPassword(admin.getPassword());
        // create the user
        U user = newUser();
        user.setEmail(admin.getEmail());
        user.setPassword(admin.getPassword());
        user.getRoles().add(AuthRoles.ADMIN);
        return save(user);
    }

    /**
     * Sends verification mail to a unverified user.
     */
    protected void sendVerificationMail(final U user) {
//        try {

            log.debug("Sending verification mail to: " + user);
        JWTClaimsSet claims = RapidJwt.create(VERIFY_AUDIENCE,
                user.getId().toString(),
                properties.getJwt().getExpirationMillis(),
                //payload
                LemonMapUtils.mapOf("email", user.getEmail()));
        String verificationCode = jweTokenService.createToken(claims);

            // make the link
            String verifyLink = properties.getApplicationUrl()
                    + "/users/" + user.getId() + "/verification?code=" + verificationCode;

            // send the mail
            sendVerificationMail(user, verifyLink);

            log.debug("Verification mail to " + user.getEmail() + " queued.");
//
//        } catch (Throwable e) {
//            // In case of exception, just log the error and keep silent
//            log.error(ExceptionUtils.getStackTrace(e));
//        }
    }

    /**
     * Sends verification mail to a unverified user.
     * Override this method if you're using a different MailData
     */
    protected void sendVerificationMail(final U user, String verifyLink) {

        mailSender.send(MailData.of(user.getEmail(),
                Message.get("com.naturalprogrammer.spring.verifySubject"),
                Message.get(
                        "com.naturalprogrammer.spring.verifyEmail", verifyLink)));
    }

    /**
     * Mails the forgot password link.
     *
     * @param user
     */
    public void sendForgotPasswordMail(U user) {

        log.debug("Mailing forgot password link to user: " + user);
        JWTClaimsSet claims = RapidJwt.create(FORGOT_PASSWORD_AUDIENCE,
                user.getEmail(),
                properties.getJwt().getExpirationMillis());
        String forgotPasswordCode = jweTokenService.createToken(claims);

        // make the link
        String forgotPasswordLink = properties.getApplicationUrl() + "/reset-password?code=" + forgotPasswordCode;

        sendForgotPasswordMail(user, forgotPasswordLink);

        log.debug("Forgot password link mail queued.");
    }


    /**
     * Mails the forgot password link.
     * <p>
     * Override this method if you're using a different MailData
     */
    public void sendForgotPasswordMail(U user, String forgotPasswordLink) {

        // send the mail
        mailSender.send(MailData.of(user.getEmail(),
                Message.get("com.naturalprogrammer.spring.forgotPasswordSubject"),
                Message.get("com.naturalprogrammer.spring.forgotPasswordEmail",
                        forgotPasswordLink)));
    }

    protected AuthorizationTokenService<RapidAuthAuthenticatedPrincipal> getAuthorizationTokenService() {
        return authorizationTokenService;
    }

    protected RapidSecurityContext<RapidAuthAuthenticatedPrincipal> getSecurityContext() {
        return securityContext;
    }

    protected AuthenticatedPrincipalFactory getAuthenticatedPrincipalFactory() {
        return authenticatedPrincipalFactory;
    }

    protected RapidPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    protected AuthProperties getProperties() {
        return properties;
    }

    protected MailSender<MailData> getMailSender() {
        return mailSender;
    }

    protected JweTokenService getJweTokenService() {
        return jweTokenService;
    }

//    protected UserService<U, ID> getRootUserService() {
//        return rootUserService;
//    }

    @Autowired
    public void injectAuthorizationTokenService(AuthorizationTokenService<RapidAuthAuthenticatedPrincipal> authorizationTokenService) {
        this.authorizationTokenService = authorizationTokenService;
    }

    @Autowired
    public void injectSecurityContext(RapidSecurityContext<RapidAuthAuthenticatedPrincipal> securityContext) {
        this.securityContext = securityContext;
    }

    @Autowired
    public void injectPasswordEncoder(RapidPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void injectProperties(AuthProperties properties) {
        this.properties = properties;
    }


    @Autowired
    public void injectMailSender(MailSender<MailData> mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired
    public void injectJweTokenService(JweTokenService jweTokenService) {
        this.jweTokenService = jweTokenService;
    }

//    @Autowired
//    @Root
//    @Lazy
//    public void injectUserService(UserService<U, ID> userService) {
//        this.rootUserService = userService;
//    }

    @Autowired
    public void injectPrincipalUserConverter(AuthenticatedPrincipalFactory authenticatedPrincipalFactory) {
        this.authenticatedPrincipalFactory = authenticatedPrincipalFactory;
    }

    //	/**
//	 * Hides the confidential fields before sending to client
//	 */
//	protected void hideConfidentialFields(U user) {
//
//		user.setPassword(null); // JsonIgnore didn't work
//		try {
//			securityChecker.checkPermission(user.getId(),user.getClass(),"WRITE");
//		}catch (AccessDeniedException e){
//			user.setEmail(null);
//		}
//
////		if (!user.hasPermission(LecwUtils.currentUser(), BasePermission.WRITE.toString()))
//
//
//		log.debug("Hid confidential fields for user: " + user);
//	}


    //    //todo I dont think that i need this. Every information needed by the client is in the UserDto he can get by sending GET user?id=myId
//    public Map<String, String> fetchFullToken(String authHeader) {
//
//        LecUtils.ensureCredentials(authorizationTokenService.parseClaim(authHeader.substring(JwtService.TOKEN_PREFIX_LENGTH),
//                AuthorizationTokenService.USER_CLAIM) == null, "com.naturalprogrammer.spring.fullTokenNotAllowed");
//
//        LemonUserDto currentUser = LecwUtils.currentUser();
//
//        Map<String, Object> claimMap = Collections.singletonMap(AuthorizationTokenService.USER_CLAIM,
//                MapperUtils.serialize(currentUser)); // Not serializing converts it to a JsonNode
//
//        Map<String, String> tokenMap = Collections.singletonMap("token", JwtService.TOKEN_PREFIX +
//                authorizationTokenService.createToken(AuthorizationTokenService.AUTH_AUDIENCE, currentUser.getEmail(),
//                        Long.valueOf(properties.getJwt().getShortLivedMillis()),
//                        claimMap));
//
//        return tokenMap;
//    }


//	@Override
//	public U update(U updatedUser, Boolean full) throws EntityNotFoundException, BadEntityException, BadEntityException {
////		// checks
////		Optional<U> byId = getRepository().findById(updatedUser.getId());
////		LexUtils.ensureFound(byId);
////		U old = byId.get();
////
////		return super.update(updatedUser, full);
//		throw new IllegalArgumentException("Call updateUser instead");
//	}

//	/**
//	 * Updates a user with the given data.
//	 */
//	@Override
//	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
//	public U updateUser(U old, U newUser) {
//
//		log.debug("Updating user: " + old);
//
//		// checks
////		LecjUtils.ensureCorrectVersion(user, updatedUser);
//
//		// delegates to updateUserFields
//		updateRoles(old, newUser, LecwUtils.currentUser());
//		U updated = getRepository().save(old);
//		log.debug("Updated user: " + old);
//
////		LemonUserDto userDto = user.toUserDto();
////		userDto.setPassword(null);
//		return updated;
//	}
}
