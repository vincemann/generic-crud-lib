package com.github.vincemann.springrapid.coredemo.controller;

import com.github.vincemann.springrapid.core.controller.CrudController;
import com.github.vincemann.springrapid.core.controller.dto.map.DtoMappings;
import com.github.vincemann.springrapid.coredemo.dto.SpecialtyDto;
import com.github.vincemann.springrapid.coredemo.model.Specialty;
import com.github.vincemann.springrapid.core.slicing.WebController;
import com.github.vincemann.springrapid.core.controller.dto.map.context.CrudDtoMappingContextBuilder;
import com.github.vincemann.springrapid.coredemo.service.SpecialtyService;

@WebController
public class SpecialtyController extends CrudController<Specialty,Long, SpecialtyService> {


    @Override
    protected DtoMappings provideDtoMappingContext(CrudDtoMappingContextBuilder builder) {
        return builder.forAll(SpecialtyDto.class)
                .build();
    }
}
