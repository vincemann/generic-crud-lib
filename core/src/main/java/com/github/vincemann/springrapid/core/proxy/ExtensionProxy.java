package com.github.vincemann.springrapid.core.proxy;

import com.github.vincemann.aoplog.GenericMatchMethodUtils;
import com.github.vincemann.springrapid.core.proxy.annotation.AutowireProxy;
import com.github.vincemann.springrapid.core.util.Lists;
import com.github.vincemann.springrapid.core.util.ProxyUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jdk invocation handler for spring rapids core component - extension proxies.
 * An extension proxy is autowired usually with a qualifier like "secured" or "acl" and represents a version of a service.
 * You can chain multiple proxies together to a proxy chain - so {@link this#getProxied()} often is another extension proxy.
 *
 * By using this concept you can autowire different version of you services.
 * For example for internal use you may autowire the default version of your service, without security checks, because only trusted internal
 * code calls the methods.
 * But in other places (like in controllers) you may want to autowire the 'secured' version of the same service, providing additional functionality (like security checks).
 * This would look something like this:
 *
 * class MyController{
 *
 *     @Autowired
 *     @Secured // this annotation must define a meta annotation of type {@link org.springframework.beans.factory.annotation.Qualifier}
 *     private MyService securedService;
 *
 *     @PostMapping(...)
 *     public void createUser(@RequestBody UserDto dto){
 *          securedService.create(dto);
 *     }
 * }
 *
 * class SomeInternalComponent{
 *
 *      @Autowired
 *      private MyService securedService;
 *
 * }
 *
 * So each proxy has its own additional functionalities, which are implemented via {@link ServiceExtension}s.
 * Each proxy has n extensions, which must implement at least one method of the proxied service.
 * The extension hooks some method of the proxied service, similar to an aop around advice.
 * Looks something like this:
 *
 * interface MyService{
 *     Object create(Object object);
 * }
 *
 * class MyServiceImpl implements MyService{
 *     ...
 * }
 *
 * @Component
 * @Scope(Prototype)
 * class OnlyAdminCanCreateExtension extends SecurityExtension<MyService> implements MyService{
 *
 *      // hook the create method by overwriting
 *      @Overwrite
 *      public Object create(Object entity){
 *          // before
 *          getAclTemplate().assertHasRole(Roles.Admin);
 *          // call next in proxy chain (could be the root object or another extension or another extension proxy)
 *          Object created = getNext().create(entity);
 *          // after
 *          return created;
 *      }
 * }
 *
 * Note that the extension must implement the proxied interface.
 * To keep things shorter you can also implement {@link CrudServiceExtension} and only overwrite the methods you want to add functionality for.
 *
 *
 * You can create the proxy and expose it to the context either programmatically by using {@link ExtensionProxyBuilder},
 * or via annotations using {@link com.github.vincemann.springrapid.core.proxy.annotation.DefineProxy}, {@link com.github.vincemann.springrapid.core.proxy.annotation.CreateProxy} and {@link AutowireProxy}
 *
 * You can also define default extensions for certain qualifiers, see {@link com.github.vincemann.springrapid.core.DefaultExtension} for more info.
 *
 *
 */
@Getter
@Setter
@Slf4j
public class ExtensionProxy implements Chain, InvocationHandler, BeanNameAware {

    //caches
    private ConcurrentHashMap<ExtensionState, Object> nextCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<MethodIdentifier, List<ExtensionHandle>> extensionChainCache = new ConcurrentHashMap<>();


    // internal stuff
    private final Map<MethodIdentifier, Method> methods = new HashMap<>();
    private List<String> ignoredMethodNames = Lists.newArrayList("getEntityClass", "getRepository", "toString", "equals", "hashCode", "getClass", "clone", "notify", "notifyAll", "wait", "finalize", "setBeanName", "getBeanName", "getTargetClass");
    private List<MethodIdentifier> learnedIgnoredMethods = new ArrayList<>();
    private ThreadLocal<State> state = new ThreadLocal<>();
    private String beanName;



    // vars
    private Object proxied;
    private List<ServiceExtension<?>> extensions = new ArrayList<>();
    private Boolean defaultExtensionsEnabled = Boolean.TRUE;
    private Set<Class<? extends ServiceExtension>> defaultExtensionsIgnored = new HashSet<>();


    public ExtensionProxy(Object proxied, ServiceExtension<?>... extensions) {
        this.proxied = proxied;
        for (Method method : proxied.getClass().getMethods()) {
            this.methods.put(new MethodIdentifier(method), method);
        }
        for (ServiceExtension<?> extension : extensions) {
            addExtension(extension);
        }
    }


    public void ignoreExtension(Class<? extends ServiceExtension> clazz) {
        defaultExtensionsIgnored.add(clazz);
        Optional<ServiceExtension<?>> added =
                getExtensions().stream().filter(e -> e.getClass().equals(clazz)).findAny();
        if (added.isPresent()) {
            log.warn("extension that should get ignored was added to proxy, removing...: " + added.get());
            removeExtension(added.get());
        }
    }

    public boolean isIgnored(Class<? extends ServiceExtension> clazz) {
        return defaultExtensionsIgnored.contains(clazz);
    }


    private void resetLearnedIgnoredMethods() {
        this.learnedIgnoredMethods.clear();
    }

    public void addExtension(ServiceExtension<?> extension) {
        Assert.isTrue(extension.matchesProxy(this),"extension does not match proxy, must be superclass of proxied object. extension: " + extension);
        this.extensions.add(extension);
        //extension expects chainController<T>, gets ChainController<S>, T is always superclass of S -> so this is safe
        extension.setChain(this);
        resetLearnedIgnoredMethods();
    }

    public void removeExtension(ServiceExtension<?> extension) {
        this.extensions.remove(extension);
        extension.setChain(null);
    }

    public void addExtension(ServiceExtension<?> extension, int index) {
        Assert.isTrue(extension.matchesProxy(this),"extension does not match proxy, must be superclass of proxied object");
        this.extensions.add(index, extension);
        //extension expects chainController<T>, gets ChainController<S>, T is always superclass of S -> so this is safe
        extension.setChain(this);
        resetLearnedIgnoredMethods();
    }


    @Override
    public Object getLast() {
        return proxied;
    }

    @Override
    public final Object invoke(Object o, Method method, Object[] args) throws Throwable {
        if (isIgnored(method)) {
            return invokeProxied(method, args);
        } else {

            try {
                if (args == null) {
                    args = new Object[]{};
                }

                List<ExtensionHandle> extensionChain = createExtensionChain(method);
                if (!extensionChain.isEmpty()) {
                    state.set(createState(o, method, args));
                    ExtensionHandle extension = extensionChain.get(0);
                    return extension.invoke(args);
                } else {
                    learnedIgnoredMethods.add(new MethodIdentifier(method));
                    return invokeProxied(method, args);
                }
            } finally {
                resetState(o, method, args);
            }
        }
    }


    protected void resetState(Object o, Method method, Object[] args) {
        state.set(createState(o, method, args));
    }

    protected State createState(Object o, Method method, Object[] args) {
        return new State(method);
    }

    /**
     * invokes {@link #proxied} object, skipping over all extensions
     * @param method method that should be invoked on proxied
     * @param args  args of method
     * @return return value of method of proxied called
     * @throws Throwable rethrows any exception the method invocation might throw
     */
    protected Object invokeProxied(Method method, Object... args) throws Throwable {
        try {
            return getMethods().get(new MethodIdentifier(method))
                    .invoke(getLast(), args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * looks through the extension chain and finds next extension that also implements currently invoked method
     * @param extension current extension acting as a base from where the next should be found
     * @return next extension from chain
     */
    @Override
    public Object getNext(ServiceExtension extension) {
        State state = this.state.get();
        // is next object cached?
        ExtensionState extensionState = new ExtensionState(state, extension);
        Object cached = nextCache.get(extensionState);
        if (cached != null) {
            return cached;
        }

        // IF STATE IS NULL - MOST LIKELY: make sure your extensions have the Prototype scope, and for each proxy a new instance of extension is added!
        // make sure extensions method is not intercepted by aop proxy maybe that triggers method call directly
        Assert.notNull(state,"method of extension: " + extension + " is called directly. Make sure to only call methods of proxies. \n Also make sure your extensions have the Prototype scope!");
        // get extension chain by method
        List<ExtensionHandle> extensionChain = extensionChainCache.get(state.getMethodIdentifier());
        Optional<ExtensionHandle> link = extensionChain.stream()
                .filter(e -> ProxyUtils.isEqual(e.getExtension(), (extension)))
                .findFirst();
        Assert.state(link.isPresent(),"already called extension: " + extension + " not part of extension chain: " + extensionChain);

        int extensionIndex = extensionChain.indexOf(link.get());
        int nextIndex = extensionIndex + 1;
        Object result;
        if (nextIndex >= extensionChain.size()) {
            // no further extension found
            result = proxied;
        } else {
            result = createProxiedExtension(extensionChain.get(nextIndex).getExtension());
        }
        nextCache.put(extensionState, result);

        return result;
    }

    // wraps extension with proxy that has proxied class (i.E. UserService)
    // the proxy simply delegates all calls to extension -> used so casting to ServiceType in Extensions work
    // it is made sure the extension always has the method in question, so the cast is safe as long as only the callee method is called
    private Object createProxiedExtension(Object extension) {
        Class<?> proxiedClass = AopUtils.getTargetClass(proxied);
        return Proxy.newProxyInstance(
                proxiedClass.getClassLoader(),
                ClassUtils.getAllInterfaces(proxiedClass).toArray(new Class[0]),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                        //this should always work, there should never get a method called, that does not exist in extension
                        try {
                            return GenericMatchMethodUtils.findMethod(extension.getClass(), method.getName(), method.getParameterTypes())
                                    .invoke(extension, objects);
                        } catch (InvocationTargetException e) {
                            throw e.getTargetException();
                        }

                    }
                });
    }

    /**
     * Each method of Proxy has extension chain that consists of all extensions of proxy, that also define given method.
     *
     * @param method method, chain should be formed for
     * @return extension chain ( list of extensions, that define method )
     */
    protected List<ExtensionHandle> createExtensionChain(Method method) {
        MethodIdentifier methodIdentifier = new MethodIdentifier(method);
        //first look in cache
        List<ExtensionHandle> extensionChain = extensionChainCache.get(methodIdentifier);
        if (extensionChain == null) {
            // start from end of extensions for start and identify all extensions having the requested method
            // all matching methods together form a chain
            // each link of the chain also saves its declared method
            Map.Entry<MethodIdentifier, List<ExtensionHandle>> method_chain_entry = new HashMap.SimpleEntry<>(methodIdentifier, new ArrayList<>());
            for (int i = 0; i < extensions.size(); i++) {
                ServiceExtension<?> extension = extensions.get(i);
                try {
                    Method extensionsMethod = GenericMatchMethodUtils.findMethod(extension.getClass(), method.getName(), method.getParameterTypes());
                    method_chain_entry.getValue().add(new ExtensionHandle(extension, extensionsMethod));
                } catch (NoSuchMethodException e) {
                    // happens all the time, when extension does not define the method in question, that may be defined in an extension
                    // further downstream tho
                    if (log.isTraceEnabled())
                        log.trace("No such method found: ", e);
                }
            }
            extensionChainCache.entrySet().add(method_chain_entry);
            extensionChain = extensionChainCache.get(methodIdentifier);
        }
        return extensionChain;
    }

    protected boolean isIgnored(Method method) {
        return getIgnoredMethodNames().contains(method.getName())
                || getLearnedIgnoredMethods().contains(new MethodIdentifier(method));
    }


    /**
     * Saved state persisting during method call of proxy.
     * Is reset after method call on {@link #proxied} finished
     */
    @EqualsAndHashCode
    private static class State {
        @Getter
        private MethodIdentifier methodIdentifier;

        public State(Method method) {
            this.methodIdentifier = new MethodIdentifier(method);
        }
    }


    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    private static class MethodIdentifier {
        String methodName;
        Class<?>[] argTypes;

        public MethodIdentifier(Method method) {
            this.methodName = method.getName();
            this.argTypes = method.getParameterTypes();
        }


    }

    // contains extension + method
    @AllArgsConstructor
    @Getter
    @ToString
    private class ExtensionHandle {
        ServiceExtension<?> extension;
        Method method;

        Object invoke(Object... args) throws Throwable {
            try {
                return method.invoke(extension, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }

        }
    }

    // only used for caching
    @AllArgsConstructor
    @EqualsAndHashCode
    private static class ExtensionState {
        private State state;
        private ServiceExtension extension;
    }


    @Override
    public String toString() {
        return "ExtensionProxy{" +
                "beanName='" + beanName + '\'' +
                ", proxied=" + proxied +
                '}';
    }
}
