package com.github.vincemann.springrapid.auth.config;

import com.github.vincemann.springrapid.auth.controller.dtoMapper.AbstractUserDtoPostProcessor;
import com.github.vincemann.springrapid.core.config.RapidDtoMapperAutoConfiguration;
import com.github.vincemann.springrapid.core.controller.dto.mapper.DtoPostProcessor;
import com.github.vincemann.springrapid.core.slicing.WebConfig;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@WebConfig
@AutoConfigureBefore(RapidDtoMapperAutoConfiguration.class)
public class RapidAuthDtoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AbstractUserDtoPostProcessor.class)
    public DtoPostProcessor abstractUserDtoPostProcessor(){
        return new AbstractUserDtoPostProcessor();
    }



}
