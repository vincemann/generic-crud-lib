package com.github.vincemann.springrapid.core.config;

import com.github.vincemann.springrapid.core.bootstrap.DatabaseDataInitManager;
import com.github.vincemann.springrapid.core.slicing.ServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

@ServiceConfig
@Slf4j
public class RapidDatabaseInitAutoConfiguration {

    public RapidDatabaseInitAutoConfiguration() {

    }

    @Bean
    public DatabaseDataInitManager databaseInitializer(){
        return new DatabaseDataInitManager();
    }
}
