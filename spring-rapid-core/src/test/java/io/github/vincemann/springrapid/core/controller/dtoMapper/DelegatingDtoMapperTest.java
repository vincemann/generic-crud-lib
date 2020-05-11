package io.github.vincemann.springrapid.core.controller.dtoMapper;

import io.github.vincemann.springrapid.core.model.IdentifiableEntityImpl;
import io.github.vincemann.springrapid.core.service.exception.BadEntityException;
import io.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DelegatingDtoMapperTest {
    DelegatingDtoMapper mapper;
    DtoMapper defaultMapper;

    DtoMapper<KnownEntity,KnownDto> customEntityMapper;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @ToString
    class KnownEntity extends IdentifiableEntityImpl<Long> {
        private String name;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @ToString
    class KnownDto extends IdentifiableEntityImpl<Long> {
        private String name;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @ToString
    class UnknownEntity extends IdentifiableEntityImpl<Long> {
        private String name;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @ToString
    class UnknownDto extends IdentifiableEntityImpl<Long> {
        private String name;
    }

    @BeforeEach
    void setUp() {
        customEntityMapper = Mockito.mock(DtoMapper.class);
        when(customEntityMapper.supports(KnownDto.class))
                .thenReturn(true);
        defaultMapper = Mockito.mock(DtoMapper.class);
        when(defaultMapper.supports(any(Class.class)))
                .thenReturn(true);
        mapper = new DelegatingDtoMapper();
        mapper.registerDelegate(customEntityMapper);
        mapper.registerDelegate(defaultMapper);
    }

    @Test
    public void mapKnownEntityToDto_shouldDelegateToKnownMapper() {
        KnownEntity entity = new KnownEntity();
        mapper.mapToDto(entity, KnownDto.class);
        verify(customEntityMapper).supports(KnownDto.class);
        verify(customEntityMapper).mapToDto(entity, KnownDto.class);
        verifyZeroInteractions(defaultMapper);
    }

    @Test
    public void mapKnownDtoToEntity_shouldDelegateToKnownMapper() throws BadEntityException, EntityNotFoundException {
        KnownDto dto = new KnownDto();
        mapper.mapToEntity(dto, KnownEntity.class);
        verify(customEntityMapper).supports(KnownDto.class);
        verify(customEntityMapper).mapToEntity(dto, KnownEntity.class);
        verifyZeroInteractions(defaultMapper);
    }

    @Test
    public void mapUnknownEntityToDto_shouldDelegateToDefaultMapper() {
        UnknownEntity entity = new UnknownEntity();
        mapper.mapToDto(entity, UnknownDto.class);
        verify(customEntityMapper).supports(UnknownDto.class);
        verifyNoMoreInteractions(customEntityMapper);
        verify(defaultMapper).mapToDto(entity,UnknownDto.class);
    }


}