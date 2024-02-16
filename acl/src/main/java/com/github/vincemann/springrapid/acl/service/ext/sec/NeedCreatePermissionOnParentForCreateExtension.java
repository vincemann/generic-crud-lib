package com.github.vincemann.springrapid.acl.service.ext.sec;

import com.github.vincemann.springrapid.acl.model.AclParentAware;
import com.github.vincemann.springrapid.acl.proxy.Secured;
import com.github.vincemann.springrapid.core.DefaultExtension;
import com.github.vincemann.springrapid.core.model.IdentifiableEntity;
import com.github.vincemann.springrapid.core.proxy.CrudServiceExtension;
import com.github.vincemann.springrapid.core.service.CrudService;
import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import org.springframework.security.acls.domain.BasePermission;

import java.io.Serializable;


public class NeedCreatePermissionOnParentForCreateExtension
        extends SecurityExtension<CrudService>
        implements CrudServiceExtension<CrudService> {


    @Override
    public IdentifiableEntity create(IdentifiableEntity entity) throws BadEntityException {
        if (entity instanceof AclParentAware){
            IdentifiableEntity<? extends Serializable> aclParent = ((AclParentAware) entity).getAclParent();
            if (aclParent == null){
                throw new BadEntityException("No acl parent found");
            }else {
                getAclTemplate().checkPermission(aclParent, BasePermission.CREATE);
            }
        }
        return getNext().create(entity);
    }
}