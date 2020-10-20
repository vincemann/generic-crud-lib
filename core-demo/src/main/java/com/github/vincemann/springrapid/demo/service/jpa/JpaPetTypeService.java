package com.github.vincemann.springrapid.demo.service.jpa;

import com.github.vincemann.springrapid.demo.repo.PetTypeRepository;
import com.github.vincemann.springrapid.demo.model.PetType;
import com.github.vincemann.springrapid.demo.service.PetTypeService;

import com.github.vincemann.springrapid.core.slicing.components.ServiceComponent;
import org.springframework.aop.TargetClassAware;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import com.github.vincemann.springrapid.core.service.JPACrudService;

@Primary
@Service
@ServiceComponent
public class JpaPetTypeService extends JPACrudService<PetType,Long, PetTypeRepository> implements PetTypeService, TargetClassAware {

    @Override
    public Class<?> getTargetClass() {
        return JpaPetTypeService.class;
    }
}
