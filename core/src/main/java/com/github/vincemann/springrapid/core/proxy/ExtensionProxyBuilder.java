package com.github.vincemann.springrapid.core.proxy;


import org.apache.commons.lang3.ClassUtils;
import org.springframework.test.util.AopTestUtils;

import java.lang.reflect.Proxy;

/**
 * Use to programmatically create {@link ExtensionProxy} proxy chains in a typesafe manner.
 *
 * For proxies that have {@link com.github.vincemann.springrapid.core.service.CrudService} as proxied object, use
 * {@link CrudServiceExtensionProxyBuilder} for additional typesafe methods.
 *
 * Example:
 *
 * @Service
 * @Root
 * public class MyService{
 *     ...
 * }
 *
 * @Configuration
 * public class MyServiceConfig {
 *
 *     @Acl
 *     @Bean
 *     public MyService myAclService(@Root MyService myRootService,
 *                                         AclExtension1 extension1,
 *                                         AclExtension2 extension2
 *     ) {
 *         return new ExtensionProxyBuilder(myRootService)
 *                 .addExtension(extension1)
 *                 .addExtension(extension2)
 *                 .build();
 *     }
 *
 *     @Secured
 *     @Bean
 *     public MyService mySecuredService(@Acl MyService myAclService,
 *                                            OnlyAdminCanCreate adminCreateExtension){
 *         // using shorter builder creation here -> {@link ExtensionProxies}
 *         return crudProxy(myAclService)
 *              .addExtension(adminCreateExtension)
 *              .build();
 *     }
 * }
 *
 *
 *
 * Note: {@link com.github.vincemann.springrapid.core.proxy.annotation.CreateProxy} and {@link com.github.vincemann.springrapid.core.proxy.annotation.DefineProxy}
 * provide an annotation-based alternative approach.
 *
 * @param <T> type of proxied service
 */
public class ExtensionProxyBuilder<T> {
    private ExtensionProxy proxy;

    public ExtensionProxyBuilder(T proxied) {
        this.proxy = new ExtensionProxy(proxied);
    }

    public ExtensionProxyBuilder<T> addExtensions(ServiceExtension<? super T>... extensions) {
        for (ServiceExtension<? super T> extension : extensions) {
            proxy.addExtension(extension);
        }
        return this;
    }

    public ExtensionProxyBuilder<T> addExtension(ServiceExtension<? super T> extension) {
        proxy.addExtension(extension);
        return this;
    }

    public ExtensionProxyBuilder<T> ignoreDefaultExtensions(Class<? extends ServiceExtension<? super T>>... extensions) {
        for (Class<? extends ServiceExtension<? super T>> extension : extensions) {
            proxy.ignoreExtension(extension);
        }
        return this;
    }

    public ExtensionProxyBuilder<T> disableDefaultExtensions(){
        return defaultExtensionsEnabled(false);
    }

    public ExtensionProxyBuilder<T> defaultExtensionsEnabled(boolean enabled) {
        proxy.setDefaultExtensionsEnabled(enabled);
        return this;
    }


    public T build() {
        T unproxied = AopTestUtils.getUltimateTargetObject(proxy.getProxied());
        T proxyInstance = (T) Proxy.newProxyInstance(
                unproxied.getClass().getClassLoader(),
                ClassUtils.getAllInterfaces(unproxied.getClass()).toArray(new Class[0]),
                proxy);
        return proxyInstance;

    }

    protected ExtensionProxy getProxy() {
        return proxy;
    }
}
