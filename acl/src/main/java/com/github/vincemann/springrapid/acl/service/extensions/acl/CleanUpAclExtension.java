package com.github.vincemann.springrapid.acl.service.extensions.acl;

import com.github.vincemann.aoplog.api.annotation.LogInteraction;
import com.github.vincemann.springrapid.core.proxy.CrudServiceExtension;
import com.github.vincemann.springrapid.core.service.CrudService;
import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

@Getter
@Slf4j
@Transactional
/**
 * Removes Acl's on delete, if existing.
 */
public class CleanUpAclExtension
        extends AbstractAclExtension<CrudService>
                implements CrudServiceExtension<CrudService>
{
    @Setter
    private boolean deleteCascade = false;


    @LogInteraction
    @Override
    public void deleteById(Serializable id) throws EntityNotFoundException {
        getNext().deleteById(id);
        aclPermissionService.deleteAclOfEntity(getEntityClass(),id,deleteCascade);
    }



}
