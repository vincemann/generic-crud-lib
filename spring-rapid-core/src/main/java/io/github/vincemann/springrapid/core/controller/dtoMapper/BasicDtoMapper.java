package io.github.vincemann.springrapid.core.controller.dtoMapper;

import io.github.vincemann.springrapid.core.model.IdentifiableEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;


/**
 * Maps a Dto to its ServiceEntity and vice versa, by using {@link ModelMapper}
 */
@Setter
@Getter
@AllArgsConstructor
public class BasicDtoMapper implements DtoMapper {

    private ModelMapper modelMapper;

    @Override
    public boolean isDtoClassSupported(Class<?> clazz) {
        return true;
    }

    @Override
    public <Dto> Dto mapToDto(IdentifiableEntity<?> source, Class<Dto> destinationClass) throws DtoMappingException {
        return modelMapper.map(source,destinationClass);
    }


    public BasicDtoMapper() {
        this.modelMapper= new ModelMapper();
    }

    @Override
    public <E extends IdentifiableEntity<?>> E mapToEntity(Object source, Class<E> destinationClass) throws DtoMappingException {
        return modelMapper.map(source,destinationClass);
    }
}
