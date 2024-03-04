package com.github.vincemann.springrapid.autobidir;

import com.github.vincemann.springrapid.autobidir.EnableAutoBiDir;
import com.github.vincemann.springrapid.autobidir.entity.RelationalEntityManager;
import com.github.vincemann.springrapid.core.model.IdentifiableEntity;
import com.github.vincemann.springrapid.core.service.CrudService;
import com.github.vincemann.springrapid.core.service.CrudServiceLocator;
import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import com.github.vincemann.springrapid.core.util.Entity;
import com.github.vincemann.springrapid.core.util.JpaUtils;
import com.github.vincemann.springrapid.core.util.NullAwareBeanUtils;
import com.github.vincemann.springrapid.core.util.ProxyUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.test.util.AopTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

@Aspect
@Slf4j
// needs to be at that order so tx advice is executed before, otherwise this code will not run within the services transaction
// and I need to rollback these changes as well then service fails -> cant user transactionTemplate
@Order(Ordered.LOWEST_PRECEDENCE)
public class RelationalEntityAdvice {

    private RelationalEntityManager relationalEntityManager;

    private CrudServiceLocator crudServiceLocator;




    @Before(
            value =
                    "com.github.vincemann.springrapid.core.RapidArchitecture.serviceOperation() && " +
                    "com.github.vincemann.springrapid.core.RapidArchitecture.deleteOperation() && " +
                    "args(id)")
    public void beforeDeleteById(JoinPoint joinPoint, Serializable id) throws EntityNotFoundException, BadEntityException {
        if (skip(joinPoint))
            return;

        assertTransactionActive();

        Optional<IdentifiableEntity> entity = findById(joinPoint, id);
        if (entity.isPresent()) {
            relationalEntityManager.delete(entity.get());
        } else {
            if (log.isWarnEnabled())
                log.warn("pre-delete auto relationship handling could not be executed, because no entity found with id:  " + id);
        }

    }

    @Before(
            value =
                    "com.github.vincemann.springrapid.core.RapidArchitecture.serviceOperation() && " +
                    "com.github.vincemann.springrapid.core.RapidArchitecture.fullUpdateOperation() && " +
                    "args(update)")
    public void beforeFullUpdate(JoinPoint joinPoint, IdentifiableEntity update) throws EntityNotFoundException, BadEntityException {
        if (skip(joinPoint))
            return;

        assertTransactionActive();

        IdentifiableEntity old = findById(joinPoint, update.getId()).get();
        IdentifiableEntity detachedOldEntity = JpaUtils.deepDetach(old);

        relationalEntityManager.fullUpdate(old, detachedOldEntity, update);
    }

    @Before(
            value =
                    "com.github.vincemann.springrapid.core.RapidArchitecture.serviceOperation() && " +
                    "com.github.vincemann.springrapid.core.RapidArchitecture.partialUpdateOperation() && " +
                    "args(update,fieldsToUpdate)")
    public void beforePartialUpdate(JoinPoint joinPoint, IdentifiableEntity update, String... fieldsToUpdate) throws EntityNotFoundException, BadEntityException {
        if (skip(joinPoint))
            return;

        assertTransactionActive();

        IdentifiableEntity old = findById(joinPoint, update.getId()).get();

        Set<String> updatedFields = Entity.findPartialUpdatedFields(update, fieldsToUpdate);

        // expects all collections to be initialized and not of Persistent Type
        IdentifiableEntity detachedOldEntity = BeanUtils.instantiateClass(ProxyUtils.getTargetClass(update));
        NullAwareBeanUtils.copyProperties(detachedOldEntity, old, updatedFields);

        relationalEntityManager.partialUpdate(old, detachedOldEntity, update, updatedFields.toArray(new String[0]));
    }

    @Before(
            value =
                    "com.github.vincemann.springrapid.core.RapidArchitecture.serviceOperation() && " +
                    "com.github.vincemann.springrapid.core.RapidArchitecture.createOperation() && " +
                    "args(entity)")
    public void beforeCreate(JoinPoint joinPoint, IdentifiableEntity entity) {
        if (skip(joinPoint))
            return;

        assertTransactionActive();

        relationalEntityManager.create(entity);
    }

    /**
     * check if join point should be skipped
     * skip if current bean is not root service
     * check if enabled by looking for {@link EnableAutoBiDir}.
     */
    protected boolean skip(JoinPoint joinPoint) {
        return !isEnabled(joinPoint);
    }

    protected boolean isEnabled(JoinPoint joinPoint){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // check for class annotation first
        boolean enabledForClass = AnnotationUtils.findAnnotation(AopProxyUtils.ultimateTargetClass(joinPoint.getTarget()), EnableAutoBiDir.class) != null;
        if (enabledForClass){
            // is enabled
            return true;
        }

        // maybe only enabled on method level
        if (method.isAnnotationPresent(EnableAutoBiDir.class))
            return true;
        return false;
    }

    protected void assertTransactionActive(){
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        if (!actualTransactionActive)
            throw new IllegalArgumentException("service method must be called within transaction, otherwise auto bidir wont work");
    }

    protected Optional<IdentifiableEntity> findById(JoinPoint joinPoint, Serializable id) {
        CrudService service = AopTestUtils.getUltimateTargetObject(joinPoint.getTarget());
        // go via crud service locator so aop is not stripped off
        return crudServiceLocator.find(service.getEntityClass()).findById(id);
    }


    @Autowired
    public void setRelationalEntityManager(RelationalEntityManager relationalEntityManager) {
        this.relationalEntityManager = relationalEntityManager;
    }

    @Autowired
    public void setCrudServiceLocator(CrudServiceLocator crudServiceLocator) {
        this.crudServiceLocator = crudServiceLocator;
    }
}