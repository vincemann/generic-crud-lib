package com.github.vincemann.springrapid.acldemo.controller.config;

import com.github.vincemann.springrapid.auth.bootstrap.AdminInitializer;
import com.github.vincemann.springrapid.core.boot.DatabaseInitializer;
import com.github.vincemann.springrapid.coretest.boot.BeforeEachTestMethodInitializable;
import com.github.vincemann.springrapid.coretest.slicing.TestConfig;
import org.springframework.context.annotation.Bean;

@TestConfig
public class AdminTestConfig {

    @Bean
    @BeforeEachTestMethodInitializable
    public DatabaseInitializer adminDatabaseDataInitializer(AdminInitializer adminInitializer){
        return adminInitializer;
    }
}
