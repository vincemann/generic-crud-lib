package com.github.vincemann.springrapid.auth.config;

import com.github.vincemann.springrapid.acl.config.RapidAclAutoConfiguration;
import com.github.vincemann.springrapid.auth.sec.GlobalRuleEnforcingAclPermissionEvaluator;
import com.github.vincemann.springrapid.auth.sec.GlobalSecurityRule;
import com.github.vincemann.springrapid.core.sec.RapidSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.model.AclService;

import java.util.List;

// permission evaluator is overridden this way
@AutoConfigureBefore({RapidAclAutoConfiguration.class})
@Configuration
public class RapidAuthAclAutoConfiguration {

    @ConditionalOnMissingBean(PermissionEvaluator.class)
    @Bean
    public PermissionEvaluator permissionEvaluator(AclService aclService, @Autowired(required = false) List<GlobalSecurityRule> globalSecurityRules, RapidSecurityContext securityContext){
        return new GlobalRuleEnforcingAclPermissionEvaluator(aclService,globalSecurityRules,securityContext);
    }
}
