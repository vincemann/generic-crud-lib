package io.github.vincemann.demo.controllers;

import io.github.vincemann.demo.dtos.pet.BasePetDto;
import io.github.vincemann.demo.dtos.pet.UpdatePetDto;
import io.github.vincemann.demo.model.Pet;
import io.github.vincemann.generic.crud.lib.config.layers.component.WebController;
import io.github.vincemann.generic.crud.lib.controller.dtoMapper.context.Direction;
import io.github.vincemann.generic.crud.lib.controller.dtoMapper.context.DtoMappingContextBuilder;
import io.github.vincemann.generic.crud.lib.controller.dtoMapper.context.DtoUsingEndpoint;
import io.github.vincemann.generic.crud.lib.controller.springAdapter.SpringAdapterJsonDtoCrudController;


@WebController
public class PetController
        extends SpringAdapterJsonDtoCrudController<Pet, Long> {

    public PetController() {
        super(DtoMappingContextBuilder.ignoreRole()
                .forAll(BasePetDto.class)
                .forEndpoint(DtoUsingEndpoint.PARTIAL_UPDATE, Direction.REQUEST, UpdatePetDto.class)
                .build()
        );
    }
}
