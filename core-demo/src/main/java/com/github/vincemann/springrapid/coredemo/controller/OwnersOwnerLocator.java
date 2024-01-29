package com.github.vincemann.springrapid.coredemo.controller;

import com.github.vincemann.springrapid.core.controller.owner.OwnerLocator;
import org.springframework.stereotype.Component;
import com.github.vincemann.springrapid.coredemo.model.Owner;

import java.util.Optional;

@Component
public class OwnersOwnerLocator implements OwnerLocator<Owner> {

    @Override
    public boolean supports(Class clazz) {
        return clazz.equals(Owner.class);
    }

    @Override
    public Optional<String> find(Owner entity) {
        if (entity.getLastName()==null){
            return Optional.empty();
        }else {
            return Optional.of(entity.getLastName());
        }
    }
}
