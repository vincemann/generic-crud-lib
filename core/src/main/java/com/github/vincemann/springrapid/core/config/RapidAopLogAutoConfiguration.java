package com.github.vincemann.springrapid.core.config;

import com.github.vincemann.aoplog.GlobalRegExMethodFilter;
import com.github.vincemann.aoplog.InvocationDescriptorFactoryImpl;
import com.github.vincemann.aoplog.ProxyAwareAopLogger;
import com.github.vincemann.aoplog.ThreadAwareIndentingLogAdapter;
import com.github.vincemann.aoplog.parseAnnotation.TypeHierarchyAnnotationParser;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Set;

import static com.github.vincemann.aoplog.Patterns.GETTER_REGEX;
import static com.github.vincemann.aoplog.Patterns.SETTER_REGEX;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Slf4j
public class RapidAopLogAutoConfiguration {

    private static final boolean SKIP_NULL_FIELDS = true;
    private static final boolean FORCE_REFLECTION = false;
    private static final int CROP_THRESHOLD = 7;
    private static final Set<String> EXCLUDE_SECURE_FIELD_NAMES = Sets.newHashSet("password");

    @ConditionalOnMissingBean(ProxyAwareAopLogger.class)
    @Bean
    public ProxyAwareAopLogger aopLogger() {
        GlobalRegExMethodFilter globalRegExMethodFilter = new GlobalRegExMethodFilter(
                GETTER_REGEX,SETTER_REGEX,"equals","hashCode","toString","^inject[A-Za-z0-9]*");
        ProxyAwareAopLogger aopLogger = new ProxyAwareAopLogger(new TypeHierarchyAnnotationParser(),new InvocationDescriptorFactoryImpl(),globalRegExMethodFilter);
        aopLogger.setLogAdapter(new ThreadAwareIndentingLogAdapter(SKIP_NULL_FIELDS, CROP_THRESHOLD, EXCLUDE_SECURE_FIELD_NAMES,FORCE_REFLECTION));
        return aopLogger;
    }
}
