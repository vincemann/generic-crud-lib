package com.github.vincemann.springrapid.autobidir.id;

import com.github.vincemann.springrapid.core.controller.dto.EntityDtoPostProcessor;
import com.github.vincemann.springrapid.core.model.IdentifiableEntity;
import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Resolves id fields, referencing parent/child entities.
 * The id resolving is done by the given {@link EntityIdResolver}s.
 *
 * @see EntityIdResolver
 */
@Order(1000)
@Transactional
public class IdResolvingDtoPostProcessor implements EntityDtoPostProcessor<Object, IdentifiableEntity<?>> {

    private List<EntityIdResolver> entityIdResolvers;
    private RelationalDtoManager relationalDtoManager;

    public IdResolvingDtoPostProcessor(List<EntityIdResolver> entityIdResolvers, RelationalDtoManager relationalDtoManager) {
        this.entityIdResolvers = entityIdResolvers;
        this.relationalDtoManager = relationalDtoManager;
    }

    @Override
    public boolean supports(Class<?>entityClass, Class<?> dtoClass) {
        Set<RelationalDtoType> relationalDtoTypes = relationalDtoManager.inferTypes(dtoClass);

        return relationalDtoTypes.contains(RelationalDtoType.BiDirChildDto)
                ||
                relationalDtoTypes.contains(RelationalDtoType.BiDirParentDto)
                ||
                relationalDtoTypes.contains(RelationalDtoType.UniDirParentDto);
    }

    @Override
    public void postProcessDto(Object dto, IdentifiableEntity<?> entity, String... fieldsToCheck) {
        //yet unfinished
        List<EntityIdResolver> entityIdResolvers = findResolvers(dto.getClass());
        for (EntityIdResolver entityIdResolver : entityIdResolvers) {
            entityIdResolver.setResolvedIds(dto, entity, fieldsToCheck);
        }
    }

    // todo this is probably the only method that needs to be transactional and also might need entityManger.merge
    @Override
    public void postProcessEntity(IdentifiableEntity<?> entity, Object dto) throws BadEntityException, EntityNotFoundException {
        //yet unfinished
        List<EntityIdResolver> entityIdResolvers = findResolvers(dto.getClass());
        for (EntityIdResolver resolver : entityIdResolvers) {
            resolver.setResolvedEntities(entity, dto);
        }
    }

    public List<EntityIdResolver> findResolvers(Class<?> dstClass) {
        List<EntityIdResolver> resolvers = new ArrayList<>();
        Set<RelationalDtoType> relationalDtoTypes = relationalDtoManager.inferTypes(dstClass);
        for (RelationalDtoType relationalDtoType : relationalDtoTypes) {
            for (EntityIdResolver entityIdResolver : entityIdResolvers) {
                if (entityIdResolver.getDtoType().equals(relationalDtoType)) {
                    resolvers.add(entityIdResolver);
                }
            }
        }

        if (resolvers.isEmpty())
            throw new IllegalArgumentException("No " + EntityIdResolver.class.getSimpleName() + " found for dstClass: " + dstClass.getSimpleName());
        return resolvers;
    }
}