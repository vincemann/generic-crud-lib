package com.github.vincemann.springrapid.auth.service;

import com.github.vincemann.springrapid.auth.AuthProperties;
import com.github.vincemann.springrapid.auth.MessageSender;
import com.github.vincemann.springrapid.auth.model.AbstractUser;
import com.github.vincemann.springrapid.auth.model.AuthRoles;
import com.github.vincemann.springrapid.auth.service.token.BadTokenException;
import com.github.vincemann.springrapid.auth.service.token.JweTokenService;
import com.github.vincemann.springrapid.auth.util.MapUtils;
import com.github.vincemann.springrapid.auth.util.RapidJwt;
import com.github.vincemann.springrapid.auth.util.TransactionalUtils;
import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import com.github.vincemann.springrapid.core.service.id.IdConverter;
import com.github.vincemann.springrapid.core.util.Entity;
import com.github.vincemann.springrapid.core.util.Message;
import com.github.vincemann.springrapid.core.util.VerifyAccess;
import com.github.vincemann.springrapid.core.util.VerifyEntity;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;
import java.util.Optional;

@Slf4j
public class ChangeContactInformationServiceImpl implements ChangeContactInformationService {

    public static final String CHANGE_CONTACT_INFORMATION_AUDIENCE = "change-contactInformation";

    private UserService userService;

    private JweTokenService jweTokenService;

    private AuthProperties properties;

    private MessageSender messageSender;

    private VerificationService verificationService;

    private IdConverter idConverter;

    @Override
    public AbstractUser changeContactInformation(String code) throws EntityNotFoundException, BadEntityException, AlreadyRegisteredException, BadTokenException {

        JWTClaimsSet claims = jweTokenService.parseToken(code);
        AbstractUser user = extractUserFromClaims(claims);

        RapidJwt.validate(claims, CHANGE_CONTACT_INFORMATION_AUDIENCE, user.getCredentialsUpdatedMillis());

        VerifyEntity.is(StringUtils.isNotBlank(user.getNewContactInformation()), "No new contactInformation found. Looks like you have already changed.");


        VerifyAccess.condition(
                claims.getClaim("newContactInformation").equals(user.getNewContactInformation()),
                Message.get("com.github.vincemann.wrong.changeContactInformationCode"));

        // Ensure that the contactInformation would be unique
        checkUniqueContactInformation(user.getNewContactInformation());
        // update the fields

        AbstractUser update = Entity.createUpdate(user);
        update.setNewContactInformation(null);
        AbstractUser updated = userService.updateContactInformation(update, user.getContactInformation());

        // make the user verified if he is not
        if (user.hasRole(AuthRoles.UNVERIFIED))
            verificationService.makeVerified(user);


        return updated;
    }


    protected AbstractUser extractUserFromClaims(JWTClaimsSet claims) throws EntityNotFoundException {
        Serializable id = idConverter.toId(claims.getSubject());
        // fetch the user
        Optional<AbstractUser> byId = userService.findById(id);
        VerifyEntity.isPresent(byId, "User with id: " + id + " not found");
        return byId.get();
    }

    @Override
    public void requestContactInformationChange(RequestContactInformationChangeDto dto) throws EntityNotFoundException, AlreadyRegisteredException, BadEntityException {
        Optional<AbstractUser> oldUser = userService.findByContactInformation(dto.getOldContactInformation());
        VerifyEntity.isPresent(oldUser, dto.getOldContactInformation(), userService.getEntityClass());


        String newContactInformation = dto.getNewContactInformation();
        checkUniqueContactInformation(newContactInformation);

//        // preserves the new contactInformation id
        AbstractUser update = Entity.createUpdate(oldUser.get());
        update.setNewContactInformation(newContactInformation);

        AbstractUser updated = userService.partialUpdate(update);

        // needs to be done bc validation exceptions are thrown after transaction ends, otherwise validation fails but
        // message is still sent
        TransactionalUtils.afterCommit(() -> sendChangeContactInformationMessage(updated));
    }

    protected void checkUniqueContactInformation(String contactInformation) throws BadEntityException {
        if (userService.findByContactInformation(contactInformation).isPresent())
            throw new BadEntityException("contact information already present");
    }

    /**
     * Mails the change-contactInformation verification link to the user.
     */
    protected void sendChangeContactInformationMessage(AbstractUser user) {
        JWTClaimsSet claims = RapidJwt.create(
                CHANGE_CONTACT_INFORMATION_AUDIENCE,
                user.getId().toString(),
                properties.getJwt().getExpirationMillis(),
                MapUtils.mapOf("newContactInformation", user.getNewContactInformation()));
        String changeContactInformationCode = jweTokenService.createToken(claims);


        log.debug("Mailing change contactInformation link to user: " + user);
        String changeContactInformationLink = UriComponentsBuilder
                .fromHttpUrl(
                        properties.getCoreProperties().getApplicationUrl()
                                + properties.getController().getChangeContactInformationUrl())
//                    .queryParam("id", user.getId())
                .queryParam("code", changeContactInformationCode)
                .toUriString();
        log.info("change contactInformation link: " + changeContactInformationLink);
        messageSender.sendMessage(changeContactInformationLink, CHANGE_CONTACT_INFORMATION_AUDIENCE, changeContactInformationCode, user.getContactInformation());

        log.debug("Change contactInformation link mail queued.");
    }
}
