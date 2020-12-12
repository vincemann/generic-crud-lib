package com.github.vincemann.springrapid.acl.service;

import com.github.vincemann.aoplog.api.AopLoggable;
import com.github.vincemann.aoplog.api.LogException;
import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.springrapid.core.model.IdentifiableEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * API for managing acl data.
 */
@Service
@Transactional
//todo test
@Slf4j
//@LogException
public class LocalPermissionService implements AopLoggable {

    private MutableAclService aclService;

    @Autowired
    public LocalPermissionService(MutableAclService aclService) {
        this.aclService = aclService;
    }


    /**
     *
     * @param targetObj
     * @param permission
     * @param username      username of user, that gains permission over target object
     */
    @LogInteraction
    public void addPermissionForUserOver(IdentifiableEntity<? extends Serializable> targetObj, Permission permission, String username) {
        final Sid sid = new PrincipalSid(username);
        addPermissionForSid(targetObj, permission, sid);
    }

    /**
     *
     * @param targetObj
     * @param permission
     * @param authority     the authority that gains the given permission over the target obj
     */
    @LogInteraction
    public void addPermissionForAuthorityOver(IdentifiableEntity<? extends Serializable> targetObj, Permission permission, String authority) {
        final Sid sid = new GrantedAuthoritySid(authority);
        addPermissionForSid(targetObj, permission, sid);
    }

    /**
     *
     * @param targetObj   inherits all permissions from parent
     * @param parent
     * @throws AclNotFoundException
     */
    @LogInteraction
    public void inheritPermissions(IdentifiableEntity<? extends Serializable> targetObj,IdentifiableEntity<? extends Serializable> parent) throws AclNotFoundException {
        final ObjectIdentity childOi = new ObjectIdentityImpl(targetObj.getClass(), targetObj.getId());
        final ObjectIdentity parentOi = new ObjectIdentityImpl(parent.getClass(), parent.getId());
//        log.debug("Entity: " + targetObj + " will inherit permissions from: " + parent);
        MutableAcl childAcl = null;
        MutableAcl parentAcl =null;

        try {
            childAcl = (MutableAcl) aclService.readAclById(childOi);
        } catch (final NotFoundException nfe) {
            childAcl = aclService.createAcl(childOi);
        }
        try {
            parentAcl = (MutableAcl) aclService.readAclById(parentOi);
        } catch (final NotFoundException nfe) {
            throw new AclNotFoundException("Acl not found for parent: " + parentOi);
        }

        childAcl.setEntriesInheriting(true);
        childAcl.setParent(parentAcl);

        log.trace("Parent Acl: " + parentAcl);
        log.trace("Child Acl before Update: " + childAcl);
        MutableAcl updated = aclService.updateAcl(childAcl);
        log.trace("Updated Child Acl: " + updated);
    }


    private void addPermissionForSid(IdentifiableEntity<? extends Serializable> targetObj, Permission permission, Sid sid) {
//        log.debug("sid: "+ sid +" will gain permission: " + PermissionUtils.toString(permission) +" over entity: " + targetObj);
        final ObjectIdentity oi = new ObjectIdentityImpl(targetObj.getClass(), targetObj.getId());

        MutableAcl acl = null;
        try {
            acl = (MutableAcl) aclService.readAclById(oi);
        } catch (final NotFoundException nfe) {
            acl = aclService.createAcl(oi);
        }
        log.trace("old acl of entity " + acl);
        acl.insertAce(acl.getEntries().size(), permission, sid, true);
        MutableAcl updated = aclService.updateAcl(acl);
        log.trace("updated acl: " + updated);
    }
}