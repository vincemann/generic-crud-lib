package com.github.vincemann.springrapid.acl;

import com.github.vincemann.springrapid.acl.service.PermissionStringConverter;
import com.github.vincemann.springrapid.core.model.IdentifiableEntity;
import com.github.vincemann.springrapid.core.sec.RapidPrincipal;
import com.github.vincemann.springrapid.core.sec.AuthorizationTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.util.SimpleMethodInvocation;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

@Slf4j
/**
 * Copied an modified from:
 * https://gist.github.com/matteocedroni/b0e5a935127316603dfb
 *
 * DefaultImpl of {@link AclTemplate}.
 * Uses {@link MethodSecurityExpressionHandler} for expression evaluation
 *
 *
 */
public class AclTemplateImpl implements AclTemplate, ApplicationContextAware {

    private Method triggerCheckMethod;
    private SpelExpressionParser parser;
    private ApplicationContext applicationContext;
    private RapidAclSecurityContext securityContext;
    private PermissionStringConverter permissionStringConverter;

    public AclTemplateImpl() {
        try {
            this.triggerCheckMethod = AclTemplateImpl.SecurityObject.class.getMethod("triggerCheck");
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e);
        }
        parser = new SpelExpressionParser();
    }

    @Override
    public <E extends IdentifiableEntity<? extends Serializable>, C extends Collection<E>> C filter(C toFilter, Permission permission) {
        AuthorizationTemplate.assertAuthenticated();
        Collection<E> filtered = new HashSet<>();
//        String permissionString = permissionStringConverter.convert(permission);
        for (E entity : toFilter) {

            AclEvaluationContext aclContext = AclEvaluationContext.builder()
                    .checkedPermission(permission)
                    .targetEntity(entity)
                    .build();
            securityContext.setAclContext(aclContext);

            boolean permitted = _checkPermission(entity.getId(), entity.getClass(), permission);
            securityContext.clearAclContext();
//            boolean permitted = checkExpression("hasPermission(" + entity.getId() + ",'" + entity.getClass().getName() + "','" + permissionString + "')");
            if (permitted) {
                filtered.add(entity);
            } else {
                if (log.isWarnEnabled())
                    log.warn("filtered out entity: " + entity);
            }
        }
        return (C) filtered;
    }

    @Override
    public void checkPermission(Serializable id, Class<?> clazz, Permission permission) {

        AclEvaluationContext aclContext = AclEvaluationContext.builder()
                .checkedPermission(permission)
                .id(id)
                .entityClass(clazz)
                .build();
        securityContext.setAclContext(aclContext);
        boolean permitted = _checkPermission(id, clazz, permission);
        securityContext.clearAclContext();
        if (!permitted) {
            RapidPrincipal principal = securityContext.currentPrincipal();
            String permissionString = permissionStringConverter.convert(permission);
            throw new AccessDeniedException("Permission not Granted! Principal: " + principal.shortToString() +
                    " does not have Permission: " + permissionString + " for entity: {" + clazz.getSimpleName() + ", id: " + id + "}");
        }


    }

    private boolean _checkPermission(Serializable id, Class<?> clazz, Permission permission) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        if (permission == null) {
            throw new IllegalArgumentException("Permission must not be null");
        }
        if (clazz == null) {
            throw new IllegalArgumentException("Clazz must not be null");
        }
        AuthorizationTemplate.assertAuthenticated();

//        RapidAuthenticatedPrincipal p = rapidSecurityContext.currentPrincipal();
        String permissionString = permissionStringConverter.convert(permission);
        return checkExpression("hasPermission(" + id + ",'" + clazz.getName() + "','" + permissionString + "')");
    }

    public void checkPermission(IdentifiableEntity<?> entity, Permission permission) throws AccessDeniedException {

        AclEvaluationContext aclContext = AclEvaluationContext.builder()
                .checkedPermission(permission)
                .targetEntity(entity)
                .build();
        securityContext.setAclContext(aclContext);

        boolean permitted = _checkPermission(entity.getId(), entity.getClass(), permission);
        if (!permitted) {
            RapidPrincipal principal = securityContext.currentPrincipal();
            String permissionString = permissionStringConverter.convert(permission);
            throw new AccessDeniedException("Permission not Granted! Principal: " + principal.shortToString() +
                    " does not have Permission: " + permissionString + " for entity: " + entity);
        }
    }


    @Override
    public boolean checkExpression(String securityExpression) {
//        if (log.isDebugEnabled()) {
//            log.debug("EVALUATING SECURITY EXPRESSION: [" + securityExpression + "]...");
//        }

        AclTemplateImpl.SecurityObject securityObject = new AclTemplateImpl.SecurityObject();
        MethodSecurityExpressionHandler expressionHandler = applicationContext.getBean(MethodSecurityExpressionHandler.class);
        //gibt dem einfach nen gemockten Methodenaufruf und nen gemocktes securityObject rein
        EvaluationContext evaluationContext = expressionHandler.createEvaluationContext(
                SecurityContextHolder.getContext().getAuthentication(),
                new SimpleMethodInvocation(securityObject, triggerCheckMethod)
        );
        boolean checkResult = ExpressionUtils.evaluateAsBoolean(parser.parseExpression(securityExpression), evaluationContext);
//        if (log.isDebugEnabled()) {
//            log.debug("SECURITY EXPRESSION EVALUATED AS: " + checkResult);
//        }
        return checkResult;
    }

    private static class SecurityObject {
        public void triggerCheck() { /*NOP*/ }
    }

    @Autowired
    public void setRapidSecurityContext(RapidAclSecurityContext rapidSecurityContext) {
        this.securityContext = rapidSecurityContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setPermissionStringConverter(PermissionStringConverter permissionStringConverter) {
        this.permissionStringConverter = permissionStringConverter;
    }

    //i dont want two ways of checking roles or authenticated
//    @LogInteraction(Severity.TRACE)
//    @Override
//    public void checkAuthenticated(){
//        boolean authenticated = checkExpression("isAuthenticated()");
//        if(!authenticated){
//            throw new AccessDeniedException("User must be authenticated");
//        }
//    }


    //@Override
////    public void checkHasRoles(String... role){
////        boolean permitted = checkExpression("hasRole('" + role + "')");
////        if(!permitted){
////            throw new AccessDeniedException("Permission not Granted! Principal : " + rapidSecurityContext.currentPrincipal()
////                    + " does not have requested role: " + role);
////        }
//    }


}