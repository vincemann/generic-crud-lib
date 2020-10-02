package com.github.vincemann.springrapid.acl.proxy;

import com.github.vincemann.springrapid.core.service.ServiceBeanType;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier("unsecured")
@Inherited
@ServiceBeanType
public @interface Unsecured {
}
