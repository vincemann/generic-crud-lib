package com.github.vincemann.springrapid.core.config;

import com.github.vincemann.springrapid.core.controller.ContextController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "rapid-core.expose-context", havingValue = "true", matchIfMissing = true)
public class RapidContextControllerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "contextController")
    public ContextController contextController(){
        return new ContextController();
    }
}