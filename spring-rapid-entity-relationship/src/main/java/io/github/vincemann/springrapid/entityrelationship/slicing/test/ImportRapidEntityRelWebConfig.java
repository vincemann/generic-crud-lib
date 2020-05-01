package io.github.vincemann.springrapid.entityrelationship.slicing.test;

import io.github.vincemann.springrapid.entityrelationship.config.EntityIdResolvingMapperAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ImportAutoConfiguration(EntityIdResolvingMapperAutoConfiguration.class)
public @interface ImportRapidEntityRelWebConfig {
}