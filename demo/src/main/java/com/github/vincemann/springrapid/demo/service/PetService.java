package com.github.vincemann.springrapid.demo.service;

import com.github.vincemann.springrapid.demo.model.Pet;
import com.github.vincemann.springrapid.demo.repo.PetRepository;
import com.github.vincemann.springrapid.core.slicing.components.ServiceComponent;
import com.github.vincemann.springrapid.core.service.CrudService;


@ServiceComponent
public interface PetService extends CrudService<Pet,Long, PetRepository> {

}