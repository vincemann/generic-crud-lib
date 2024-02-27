package com.github.vincemann.springrapid.acldemo.service;

import com.github.vincemann.springrapid.acldemo.MyRoles;
import com.github.vincemann.springrapid.acldemo.dto.owner.SignupOwnerDto;
import com.github.vincemann.springrapid.acldemo.dto.vet.SignupVetDto;
import com.github.vincemann.springrapid.acldemo.model.Owner;
import com.github.vincemann.springrapid.acldemo.model.Vet;
import com.github.vincemann.springrapid.auth.service.VerificationService;
import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import com.google.common.collect.Sets;

public class MySignupServiceImpl implements MySignupService {

    private OwnerService ownerService;
    private VetService vetService;
    private VerificationService verificationService;

    @Override
    public Owner signupOwner(SignupOwnerDto dto) throws BadEntityException {
        Owner owner = Owner.builder()
                .address(dto.getAddress())
                .city(dto.getCity())
                .roles(Sets.newHashSet(MyRoles.OWNER,MyRoles.USER))
                .build();

        Owner saved = ownerService.create(owner);
        try {
            verificationService.makeUnverified(saved);
        } catch (EntityNotFoundException e) {
            throw new RuntimeException(e);
        }
        return saved;
    }

    @Override
    public Vet signupVet(SignupVetDto dto) {
        return null;
    }
}
