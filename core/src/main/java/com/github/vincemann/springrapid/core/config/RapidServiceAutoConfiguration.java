package com.github.vincemann.springrapid.core.config;

import com.github.vincemann.springrapid.core.service.context.*;
import com.github.vincemann.springrapid.core.service.locator.CrudServiceLocator;
import com.github.vincemann.springrapid.core.service.locator.CrudServiceLocatorImpl;
import com.github.vincemann.springrapid.core.slicing.ServiceConfig;
import com.github.vincemann.springrapid.core.util.EntityLocator;
import com.github.vincemann.springrapid.core.util.LazyToStringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Scope;

import javax.persistence.EntityManager;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@ServiceConfig
public class RapidServiceAutoConfiguration {

    @Autowired(required = false)
    EntityManager entityManager;


    static int count = 0;
    @Bean
    @ConditionalOnMissingBean(ServiceCallContextAdvice.class)
    public ServiceCallContextAdvice serviceCallContextAdvice(){
        System.err.println("creating service call context advice " + count++);
        return new ServiceCallContextAdvice();
    }


    @ConditionalOnMissingBean(CrudServiceLocator.class)
    @Bean
    public CrudServiceLocator crudServiceLocator(ConfigurableApplicationContext context, ConfigurableListableBeanFactory beanFactory){
        CrudServiceLocatorImpl csl = new CrudServiceLocatorImpl();
        context.addBeanFactoryPostProcessor(csl);
        csl.postProcessBeanFactory(beanFactory);
        return csl;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ServiceCallContext serviceCallContext(){
        return new ServiceCallContext();
    }


    @Bean
    @ConditionalOnMissingBean(EntityLocator.class)
    public EntityLocator entityLocator(){
        return new EntityLocator();
    }

    @Autowired
    public void configureLazyToStringUtil(){
        LazyToStringUtil.setEntityManager(entityManager);
    }

    @Bean
    @ConditionalOnMissingBean(ServiceCallContextFactory.class)
    public ServiceCallContextFactory serviceCallContextFactory(){
        return new DefaultServiceCallContextFactory();
    }



}
