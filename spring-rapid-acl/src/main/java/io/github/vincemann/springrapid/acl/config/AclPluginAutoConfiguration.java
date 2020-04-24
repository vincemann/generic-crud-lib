package io.github.vincemann.springrapid.acl.config;

import io.github.vincemann.springrapid.acl.plugin.AdminFullAccessAclPlugin;
import io.github.vincemann.springrapid.acl.plugin.AuthenticatedFullAccessAclPlugin;
import io.github.vincemann.springrapid.acl.plugin.InheritParentAclPlugin;
import io.github.vincemann.springrapid.acl.service.LocalPermissionService;
import io.github.vincemann.springrapid.core.config.RapidJacksonAutoConfiguration;
import io.github.vincemann.springrapid.core.slicing.config.ServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.acls.model.MutableAclService;

@ServiceConfig
@Slf4j
@AutoConfigureAfter(RapidJacksonAutoConfiguration.class)
public class AclPluginAutoConfiguration {

    public AclPluginAutoConfiguration() {
        log.info("Created");
    }


    @ConditionalOnMissingBean(LocalPermissionService.class)
    @Bean
    public LocalPermissionService localPermissionService(MutableAclService aclService){
        return new LocalPermissionService(aclService);
    }

    @ConditionalOnMissingBean(AdminFullAccessAclPlugin.class)
    @Bean
    public AdminFullAccessAclPlugin adminFullAccessAclPlugin(MutableAclService mutableAclService){
        return new AdminFullAccessAclPlugin(localPermissionService(mutableAclService),mutableAclService);
    }

    @ConditionalOnMissingBean(AuthenticatedFullAccessAclPlugin.class)
    @Bean
    public AuthenticatedFullAccessAclPlugin authenticatedFullAccessAclPlugin(MutableAclService mutableAclService){
        return new AuthenticatedFullAccessAclPlugin(localPermissionService(mutableAclService),mutableAclService);
    }

    @Bean
    @ConditionalOnMissingBean(InheritParentAclPlugin.class)
    public InheritParentAclPlugin inheritParentAclPlugin(MutableAclService aclService){
        return new InheritParentAclPlugin(localPermissionService(aclService),aclService);
    }
}
