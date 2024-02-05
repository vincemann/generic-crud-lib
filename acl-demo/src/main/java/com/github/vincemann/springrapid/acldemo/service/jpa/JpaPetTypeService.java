package com.github.vincemann.springrapid.acldemo.service.jpa;

import com.github.vincemann.springrapid.core.service.JpaCrudService;
import com.github.vincemann.springrapid.acldemo.model.PetType;
import com.github.vincemann.springrapid.acldemo.repo.PetTypeRepository;
import com.github.vincemann.springrapid.acldemo.service.PetTypeService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class JpaPetTypeService
        extends JpaCrudService<PetType,Long, PetTypeRepository>
            implements PetTypeService {

    @Override
    public Class<?> getTargetClass() {
        return JpaPetTypeService.class;
    }
}
