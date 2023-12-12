package com.github.vincemann.springrapid.acldemo.config;

import com.github.vincemann.springrapid.acl.DefaultSecurityExtension;
import com.github.vincemann.springrapid.acl.service.extensions.acl.RoleHasPermissionAboutSavedAclExtension;
import com.github.vincemann.springrapid.acl.service.extensions.security.NeedCreatePermissionOnParentForSaveExtension;
import com.github.vincemann.springrapid.acldemo.service.extensions.OnlyRoleAndAdminCanSaveSecurityExtension;
import com.github.vincemann.springrapid.acldemo.auth.MyRoles;
import com.github.vincemann.springrapid.core.slicing.ServiceConfig;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.security.acls.domain.BasePermission;

@ServiceConfig
public class ServiceExtensionsConfig {

    @Bean
    public RoleHasPermissionAboutSavedAclExtension vetHasFullPermissionAboutSavedAclExtension(){
        return new RoleHasPermissionAboutSavedAclExtension(MyRoles.VET, BasePermission.ADMINISTRATION);
    }

    @Bean
    public OnlyRoleAndAdminCanSaveSecurityExtension onlyVetAndAdminCanCreateSecurityExtension(){
        return new OnlyRoleAndAdminCanSaveSecurityExtension(MyRoles.VET);
    }

    @Bean
    public RoleHasPermissionAboutSavedAclExtension vetsHaveReadPermissionAboutSavedAclExtension(){
        return new RoleHasPermissionAboutSavedAclExtension(MyRoles.VET,BasePermission.READ);
    }

    @ConditionalOnMissingBean(name = "needCreatePermissionOnParentForSaveExtension")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @DefaultSecurityExtension
    @Bean
    public NeedCreatePermissionOnParentForSaveExtension needCreatePermissionOnParentForSaveExtension(){
        return new NeedCreatePermissionOnParentForSaveExtension();
    }
}
