package io.github.vincemann.generic.crud.lib.controller;

import io.github.vincemann.generic.crud.lib.controller.dtoMapper.EntityMappingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.github.vincemann.generic.crud.lib.controller.dtoMapper.DtoMapper;
import io.github.vincemann.generic.crud.lib.model.IdentifiableEntity;
import io.github.vincemann.generic.crud.lib.service.CrudService;
import io.github.vincemann.generic.crud.lib.service.exception.BadEntityException;
import io.github.vincemann.generic.crud.lib.service.exception.EntityNotFoundException;
import io.github.vincemann.generic.crud.lib.service.exception.NoIdException;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;


/**
 * Impl of {@link DTOCrudController} that handles the following:
 * Mapping of ServiceEntity to DTO and vice versa.
 * Interaction with specified  {@link CrudService}.
 * Supply callback Methods.
 *
 * @param <ServiceE> Service Entity Type, of entity, which curd enpoints are exposed by this Controller
 * @param <Service>  Service Type of {@link ServiceE}
 * @param <DTO>      DTO Type corresponding to {@link ServiceE}
 * @param <Id>       Id Type of {@link ServiceE}
 */
public abstract class BasicDTOCrudController<ServiceE extends IdentifiableEntity<Id>,DTO extends IdentifiableEntity<Id>,  Id extends Serializable & Comparable, Service extends CrudService<ServiceE, Id>> implements DTOCrudController<DTO, Id> {

    private Service crudService;
    private DtoMapper dtoMapper;
    @SuppressWarnings("unchecked")
    private Class<ServiceE> serviceEntityClass = (Class<ServiceE>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    @SuppressWarnings("unchecked")
    private Class<DTO> dtoClass = (Class<DTO>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    public BasicDTOCrudController(Service crudService, DtoMapper dtoMapper) {
        this.crudService = crudService;
        this.dtoMapper = dtoMapper;
    }

    //todo methoden einbauen die einfach nur die id returnen

    @SuppressWarnings("unchecked")
    public ResponseEntity<DTO> find(Id id) throws NoIdException, EntityNotFoundException {
        Optional<ServiceE> optionalEntity = crudService.findById(beforeFindEntity(id));
        //noinspection OptionalIsPresent
        if (optionalEntity.isPresent()) {
            return ok(getDtoMapper().mapServiceEntityToDto(afterFindEntity(optionalEntity.get()),dtoClass));
        } else {
            throw new EntityNotFoundException();
        }
    }


    protected Id beforeFindEntity(Id id) {
        return id;
    }

    protected ServiceE afterFindEntity(ServiceE foundEntity) {
        return foundEntity;
    }

    @SuppressWarnings("unchecked")
    public ResponseEntity<DTO> create(DTO dto) throws BadEntityException, EntityMappingException {
        ServiceE serviceEntity = getDtoMapper().mapDtoToServiceEntity(dto,serviceEntityClass);
        ServiceE savedServiceEntity = crudService.save(beforeCreateEntity(serviceEntity, dto));
        return new ResponseEntity(getDtoMapper().mapServiceEntityToDto(afterCreateEntity(savedServiceEntity),dtoClass), HttpStatus.OK);
    }


    protected ServiceE beforeCreateEntity(ServiceE entity, DTO dto) {
        return entity;
    }

    protected ServiceE afterCreateEntity(ServiceE entity) {
        return entity;
    }

    @SuppressWarnings("unchecked")
    public ResponseEntity<DTO> update(DTO dto) throws BadEntityException, EntityMappingException, NoIdException, EntityNotFoundException {
        ServiceE serviceEntity = getDtoMapper().mapDtoToServiceEntity(dto,serviceEntityClass);
        ServiceE updatedServiceEntity = crudService.update(beforeUpdateEntity(serviceEntity));
        //no idea why casting is necessary here?
        return new ResponseEntity(getDtoMapper().mapServiceEntityToDto(afterUpdateEntity(updatedServiceEntity),dtoClass), HttpStatus.OK);
    }

    protected ServiceE beforeUpdateEntity(ServiceE entity) {
        return entity;
    }

    protected ServiceE afterUpdateEntity(ServiceE entity) {
        return entity;
    }


    public ResponseEntity delete(Id id) throws NoIdException, EntityNotFoundException {
        crudService.deleteById(beforeDeleteEntity(id));
        afterDeleteEntity(id);
        return ResponseEntity.ok().build();
    }

    protected Id beforeDeleteEntity(Id id) {
        return id;
    }

    protected void afterDeleteEntity(Id id) {
    }


    private ResponseEntity<DTO> ok(DTO entity) {
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    public Service getCrudService() {
        return crudService;
    }

    public DtoMapper getDtoMapper() {
        return dtoMapper;
    }

    public Class<ServiceE> getServiceEntityClass() {
        return serviceEntityClass;
    }

    public Class<DTO> getDtoClass() {
        return dtoClass;
    }
}
