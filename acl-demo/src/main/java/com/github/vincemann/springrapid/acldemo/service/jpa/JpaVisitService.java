package com.github.vincemann.springrapid.acldemo.service.jpa;

import com.github.vincemann.springrapid.acl.proxy.Acl;
import com.github.vincemann.springrapid.acl.proxy.Secured;
import com.github.vincemann.springrapid.acl.service.AceNotFoundException;
import com.github.vincemann.springrapid.acl.service.AclNotFoundException;
import com.github.vincemann.springrapid.acl.service.AclPermissionService;
import com.github.vincemann.springrapid.acldemo.model.Owner;
import com.github.vincemann.springrapid.acldemo.repo.OwnerRepository;
import com.github.vincemann.springrapid.acldemo.repo.VisitRepository;
import com.github.vincemann.springrapid.acldemo.service.OwnerService;
import com.github.vincemann.springrapid.core.proxy.annotation.CreateProxy;
import com.github.vincemann.springrapid.core.proxy.annotation.DefineProxy;
import com.github.vincemann.springrapid.core.service.JPACrudService;
import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import com.github.vincemann.springrapid.core.util.VerifyEntity;
import org.springframework.stereotype.Component;
import com.github.vincemann.springrapid.acldemo.model.Visit;
import com.github.vincemann.springrapid.acldemo.service.VisitService;
import org.springframework.aop.TargetClassAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Service;

import java.util.Optional;


@DefineProxy(name = "acl", extensions = {
        "authenticatedGainsAdminPermissionAboutSavedAclExtension",
        "vetsGainReadPermissionAboutSavedAclExtension",
        "ownerGainsReadPermissionForSavedVisitsAclExtension"
})
@DefineProxy(name = "secured", extensions = {
        "onlyVetAndAdminCanCreateSecurityExtension",
        "vetCanOnlySaveOwnVisitsSecurityExtension",
        "needCreatePermissionForSubscribingSecurityExtension"
})
@CreateProxy(qualifiers = Acl.class,proxies = "acl")
@CreateProxy(qualifiers = Secured.class,proxies = {"acl","secured"})
@Primary
@Service
@Component
public class JpaVisitService extends JPACrudService<Visit,Long, VisitRepository>
        implements VisitService, TargetClassAware {


    private AclPermissionService aclPermissionService;
    private OwnerService ownerService;
    private final OwnerRepository ownerRepository;

    public JpaVisitService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Override
    public void subscribeOwner(Long ownerId, Long visitId) throws EntityNotFoundException {
        Optional<Owner> ownerById = ownerService.findById(ownerId);
        Owner owner = VerifyEntity.isPresent(ownerById, ownerId, Owner.class);
        Visit visit = VerifyEntity.isPresent(service.findById(visitId), visitId, Visit.class);

        aclPermissionService.savePermissionForUserOverEntity(owner.getUser().getContactInformation(),visit, BasePermission.READ);
    }

    @Override
    public void unsubscribeOwner(Long ownerId, Long visitId) throws BadEntityException, EntityNotFoundException {
        try {
            Optional<Owner> ownerById = ownerService.findById(ownerId);
            Owner owner = VerifyEntity.isPresent(ownerById, ownerId, Owner.class);
            Visit visit = VerifyEntity.isPresent(service.findById(visitId), visitId, Visit.class);

            aclPermissionService.deletePermissionForUserOverEntity(owner.getUser().getContactInformation(),visit, BasePermission.READ);
        } catch (AclNotFoundException | AceNotFoundException e) {
            throw new BadEntityException(e);
        }
    }

    @Autowired
    public void setOwnerService(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @Autowired
    public void injectAclPermissionService(AclPermissionService aclPermissionService) {
        this.aclPermissionService = aclPermissionService;
    }

    @Override
    public Class<?> getTargetClass() {
        return JpaVisitService.class;
    }
}

