package com.github.vincemann.springrapid.acldemo.controller;

import com.github.vincemann.springrapid.acl.SecuredCrudController;
import com.github.vincemann.springrapid.acldemo.dto.VisitDto;
import com.github.vincemann.springrapid.acldemo.model.Owner;
import com.github.vincemann.springrapid.acldemo.model.Visit;
import com.github.vincemann.springrapid.acldemo.service.OwnerService;
import com.github.vincemann.springrapid.acldemo.service.VisitService;
import com.github.vincemann.springrapid.core.controller.dto.map.DtoMappingsBuilder;
import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import com.github.vincemann.springrapid.core.util.VerifyEntity;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

import static com.github.vincemann.springrapid.core.controller.dto.map.DtoMappingConditions.any;

@Controller
public class VisitController
        extends SecuredCrudController<Visit, Long>
{
    private OwnerService ownerService;
    @Getter
    private String subscribeOwnerUrl;
    private VisitService service;

    @Override
    protected void configureDtoMappings(DtoMappingsBuilder builder) {
        builder.when(any()).thenReturn(VisitDto.class);
    }

    @Autowired
    @Lazy
    public void setService(VisitService service) {
        this.service = service;
    }

    @Override
    protected void initUrls() {
        super.initUrls();
        this.subscribeOwnerUrl = getEntityBaseUrl() + "subscribe-owner";
    }

    @RequestMapping(value = "/api/core/visit/subscribe-owner", method = RequestMethod.GET)
    public ResponseEntity<?> subscribeOwner(@RequestParam(value = "subscribe") boolean subscribe, @RequestParam("ownerid") long ownerId, @RequestParam("visitid") long visitId) throws BadEntityException, EntityNotFoundException {
        if (subscribe){
            service.subscribeOwner(ownerId,visitId);
        }else {
            service.unsubscribeOwner(ownerId,visitId);
        }
        return ResponseEntity.ok().build();
    }


    @Autowired
    public void injectOwnerService(OwnerService ownerService) {
        this.ownerService = ownerService;
    }
}
