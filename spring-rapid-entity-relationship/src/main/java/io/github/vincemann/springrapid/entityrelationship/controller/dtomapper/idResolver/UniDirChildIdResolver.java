package io.github.vincemann.springrapid.entityrelationship.controller.dtomapper.idResolver;

import io.github.vincemann.springrapid.core.controller.dtoMapper.DtoMappingException;
import io.github.vincemann.springrapid.entityrelationship.dto.uniDir.UniDirChildDto;
import io.github.vincemann.springrapid.core.model.IdentifiableEntity;
import io.github.vincemann.springrapid.entityrelationship.model.uniDir.child.UniDirChild;
import io.github.vincemann.springrapid.core.service.locator.CrudServiceLocator;

import java.io.Serializable;
import java.util.Map;

public class UniDirChildIdResolver extends EntityIdResolver<UniDirChild, UniDirChildDto> {

    public UniDirChildIdResolver(CrudServiceLocator crudServiceLocator) {
        super(crudServiceLocator, UniDirChildDto.class);
    }

    public void resolveEntityIds(UniDirChild mappedUniDirChild, UniDirChildDto uniDirChildDto) throws DtoMappingException {
        try {
            Map<Class, Serializable> allParentIdToClassMappings = uniDirChildDto.findAllUniDirParentIds();
            for (Map.Entry<Class, Serializable> parentIdToClassMapping : allParentIdToClassMappings.entrySet()) {
                Object parent = findEntityFromService(parentIdToClassMapping);
                try {
                    //set parent of mapped child
                    mappedUniDirChild._findAndSetParent(parent);
                    //dont set backref, because Parent does not know about Child (uniDir)
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Found Parent " + parent + " is not of Type UniDirParent");
                }
            }
        } catch (IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void resolveDtoIds(UniDirChildDto mappedDto, UniDirChild serviceEntity ){
        try {
            for (Object uniDirParent : serviceEntity._findParents()) {
                mappedDto.addUniDirParentsId((IdentifiableEntity) uniDirParent);
            }
        }catch (IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }
}
