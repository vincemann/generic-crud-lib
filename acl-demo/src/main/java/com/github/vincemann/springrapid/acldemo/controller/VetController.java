package com.github.vincemann.springrapid.acldemo.controller;

import com.github.vincemann.springrapid.acl.SecuredCrudController;
import com.github.vincemann.springrapid.acldemo.MyRoles;
import com.github.vincemann.springrapid.acldemo.dto.vet.CreateVetDto;
import com.github.vincemann.springrapid.acldemo.dto.vet.VetUpdatesOwnDto;
import com.github.vincemann.springrapid.core.controller.dto.map.context.CrudDtoMappingContextBuilder;
import com.github.vincemann.springrapid.core.controller.dto.map.Direction;
import com.github.vincemann.springrapid.core.controller.dto.map.DtoMappings;
import com.github.vincemann.springrapid.core.controller.dto.map.DtoRequestInfo;
import com.github.vincemann.springrapid.core.security.Roles;
import com.github.vincemann.springrapid.core.slicing.WebController;
import com.github.vincemann.springrapid.acldemo.dto.vet.FullVetDto;
import com.github.vincemann.springrapid.acldemo.model.Vet;
import com.github.vincemann.springrapid.acldemo.service.VetService;

@WebController
public class VetController
        extends SecuredCrudController<Vet, Long, VetService> {

    @Override
    protected DtoMappings provideDtoMappingContext(CrudDtoMappingContextBuilder builder) {
        return builder

                .withAllRoles()
                .withAllPrincipals()
                .forEndpoint(getCreateUrl(), CreateVetDto.class)



                .withRoles(MyRoles.VET)
                .withPrincipal(DtoRequestInfo.Principal.OWN)
                .forUpdate(Direction.REQUEST, VetUpdatesOwnDto.class)


                .withAllRoles()
                .withAllPrincipals()
                .forResponse(FullVetDto.class)

                // admin can link specialties to vets
                .withRoles(Roles.ADMIN)
                .withAllPrincipals()
                .forAll(FullVetDto.class)

                .build();
    }
}
