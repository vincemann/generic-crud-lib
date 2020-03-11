package io.github.vincemann.generic.crud.lib.test.service.result.matcher.compare.resolve;

import io.github.vincemann.generic.crud.lib.model.IdentifiableEntity;
import io.github.vincemann.generic.crud.lib.test.service.result.ServiceTestContext;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public class BasicCompareEntityPlaceholderResolver implements CompareEntityPlaceholderResolver {

    @Override
    public IdentifiableEntity resolve(CompareEntityPlaceholder compareEntityPlaceholder, ServiceTestContext testContext) {
        switch (compareEntityPlaceholder) {
            case DB_ENTITY:
                Serializable id = findDbEntityId(testContext);
                Optional<IdentifiableEntity> byId = testContext.getRepository().findById(id);
                if (byId.isPresent()) {
                    return byId.get();
                }
            case SERVICE_INPUT_ENTITY:
                IdentifiableEntity firstEntity = findFirstEntity(testContext.getServiceResult().getServiceRequest().getArgs());
                if(firstEntity==null){
                    throw new IllegalArgumentException("Could not find Service Input entity");
                }
            case SERVICE_RETURNED_ENTITY:
                Object result =  testContext.getServiceResult().getResult();
                if(result instanceof IdentifiableEntity){
                    return ((IdentifiableEntity) result);
                }else if (result instanceof Optional){
                    return (IdentifiableEntity) ((Optional) result).get();
                }else{
                    throw new IllegalArgumentException("Result Entity is of wrong type: " + result.getClass());
                }
        }
        throw new IllegalArgumentException("Could not resolve ComapareEntityResolvable");
    }

    /**
     * finds id from service result entity or first Entity arg
     *
     * @param testContext
     * @return
     */
    private Serializable findDbEntityId(ServiceTestContext testContext) {
        Object result = testContext.getServiceResult().getResult();
        if(result instanceof Optional){
            result = ((Optional) result).get();
        }
        if (result instanceof IdentifiableEntity) {
            Serializable returnedEntityId = ((IdentifiableEntity) result).getId();
            if (returnedEntityId != null) {
                return returnedEntityId;
            }
        }
        List<Object> args = testContext.getServiceResult().getServiceRequest().getArgs();
        if (args != null) {
            IdentifiableEntity firstEntity = findFirstEntity(args);
            if(firstEntity!=null) {
                if (firstEntity.getId() != null) {
                    return firstEntity.getId();
                }
            }
        }
        throw new IllegalArgumentException("Id for db Entity could not be found");
    }

    private IdentifiableEntity findFirstEntity(List<Object> args){
        for (Object arg : args) {
            if (arg instanceof IdentifiableEntity) {
                return ((IdentifiableEntity) arg);
            }
        }
        return null;
    }
}
