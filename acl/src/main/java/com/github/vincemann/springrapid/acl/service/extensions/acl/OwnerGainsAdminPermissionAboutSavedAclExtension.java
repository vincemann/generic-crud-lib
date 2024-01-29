package com.github.vincemann.springrapid.acl.service.extensions.acl;

import org.springframework.stereotype.Component;
import org.springframework.security.acls.domain.BasePermission;

@Component
public class OwnerGainsAdminPermissionAboutSavedAclExtension extends OwnerGainsPermissionAboutSavedAclExtension {
    public OwnerGainsAdminPermissionAboutSavedAclExtension() {
        super(BasePermission.ADMINISTRATION);
    }


}
