package com.github.vincemann.springrapid.authdemo.config;

import com.github.vincemann.springrapid.authtests.adapter.AuthTestAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.github.vincemann.springrapid.authdemo")
public class MyTestConfig {

    @Bean
    public AuthTestAdapter authTestAdapter(){
        return new MyAuthTestAdapter();
    }


}
