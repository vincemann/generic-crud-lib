package com.github.vincemann.springrapid.core.proxy;

import com.github.vincemann.aoplog.api.annotation.LogInteraction;
import com.github.vincemann.springrapid.core.model.IdentifiableEntity;
import com.github.vincemann.springrapid.core.service.CrudService;
import com.github.vincemann.springrapid.core.service.JPACrudService;
import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;


//override config from CrudService -> explicitly enable Logging for methods that actually matter
@LogInteraction(disabled = true)
public interface CrudServiceExtension<S extends CrudService>
            extends CrudService, NextLinkAware<S>{


    @Override
    default Optional findById(Serializable id) {
        return getNext().findById(id);
    }

    @Override
    default IdentifiableEntity partialUpdate(IdentifiableEntity entity, String... fieldsToRemove) throws EntityNotFoundException, BadEntityException {
//        System.err.println("invokoing this " + this.getClass().getSimpleName() +" partialUpdate " + JPACrudService.count++);
        return getNext().partialUpdate(entity,fieldsToRemove);
    }

//    @Override
//    default IdentifiableEntity partialUpdate(IdentifiableEntity update, Set<String> collectionsToUpdate, String... fieldsToRemove) throws EntityNotFoundException, BadEntityException {
//        return getNext().partialUpdate(update,collectionsToUpdate,fieldsToRemove);
//    }


    @Override
    default Set findAll(Set filters) {
        return getNext().findAll(filters);
    }

    @Override
    default Set<IdentifiableEntity> findSome(Set ids) {
        return getNext().findSome(ids);
    }

    // i dont know why i have to use raw Set Type
    @Override
    default IdentifiableEntity partialUpdate(IdentifiableEntity update, Set propertiesToUpdate, String... fieldsToRemove) throws EntityNotFoundException, BadEntityException {
        return getNext().partialUpdate(update, propertiesToUpdate,fieldsToRemove);
    }

    @Override
    default IdentifiableEntity fullUpdate(IdentifiableEntity entity) throws BadEntityException, EntityNotFoundException {
        return getNext().fullUpdate(entity);
    }

    @Override
    default IdentifiableEntity save(IdentifiableEntity entity) throws BadEntityException {
        return getNext().save(entity);
    }

    @Override
    default void deleteById(Serializable id) throws EntityNotFoundException {
        getNext().deleteById(id);
    }

    @Override
    default IdentifiableEntity softUpdate(IdentifiableEntity entity) throws EntityNotFoundException, BadEntityException {
        return getNext().softUpdate(entity);
    }

    @Override
    public default Set<IdentifiableEntity> findAll() {
        return getNext().findAll();
    }

    // todo change to getLast()
    @Override
    public default Class<IdentifiableEntity> getEntityClass() {
//        return getNext().getEntityClass();
        return getNext().getEntityClass();
    }


    //    @Override
//    public default CrudRepository getRepository() {
//        return getNext().getRepository();
//    }

}
