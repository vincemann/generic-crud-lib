package com.github.vincemann.springrapid.syncdemo.service.jpa;

import com.github.vincemann.springrapid.core.service.JPACrudService;
import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.slicing.ServiceComponent;
import com.github.vincemann.springrapid.syncdemo.model.Pet;
import com.github.vincemann.springrapid.syncdemo.repo.PetRepository;
import com.github.vincemann.springrapid.syncdemo.service.PetService;
import com.github.vincemann.springrapid.syncdemo.service.Root;
import org.springframework.aop.TargetClassAware;
import org.springframework.stereotype.Service;

import java.util.Set;

@Root
@Service
@ServiceComponent
public class JpaPetService extends JPACrudService<Pet, Long, PetRepository> implements PetService, TargetClassAware {

    @Override
    public Class<?> getTargetClass() {
        return JpaPetService.class;
    }

    @Override
    public Set<Pet> findAllOfParent(Long parentId) {
        return getRepository().findAllByOwnerId(parentId);
    }
}