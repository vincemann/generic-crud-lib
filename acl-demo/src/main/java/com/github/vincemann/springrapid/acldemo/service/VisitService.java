package com.github.vincemann.springrapid.acldemo.service;

import com.github.vincemann.springrapid.acldemo.dto.visit.CreateVisitDto;
import com.github.vincemann.springrapid.acldemo.model.Visit;
import com.github.vincemann.springrapid.auth.ex.BadEntityException;
import com.github.vincemann.springrapid.auth.ex.EntityNotFoundException;

import java.util.Optional;

public interface VisitService {


    Visit create(CreateVisitDto dto) throws BadEntityException, EntityNotFoundException;
    void addSpectator(Long spectatorId, Long visitId) throws EntityNotFoundException;
    void removeSpectator(Long spectatorId, Long visitId) throws EntityNotFoundException;

    Optional<Visit> find(long id);
}
