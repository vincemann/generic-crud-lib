package com.github.vincemann.springrapid.authdemo.service;

import com.github.vincemann.springrapid.core.service.CrudService;
import com.github.vincemann.springrapid.core.slicing.components.ServiceComponent;
import com.github.vincemann.springrapid.authdemo.model.Owner;

import java.util.Optional;



@ServiceComponent
public interface OwnerService extends CrudService<Owner,Long> {
    Optional<Owner> findByLastName(String lastName);
    public Optional<Owner> findOwnerOfTheYear();
}
