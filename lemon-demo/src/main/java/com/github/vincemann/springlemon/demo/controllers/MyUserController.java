package com.github.vincemann.springlemon.demo.controllers;

import com.github.vincemann.springlemon.auth.controller.AbstractUserController;
import com.github.vincemann.springlemon.auth.controller.LemonDtoEndpoint;
import com.github.vincemann.springlemon.auth.controller.LemonDtoMappingContextBuilder;
import com.github.vincemann.springlemon.demo.domain.User;
import com.github.vincemann.springlemon.demo.domain.MySignupForm;
import com.github.vincemann.springlemon.demo.dto.AdminUpdateUserDto;
import com.github.vincemann.springlemon.demo.dto.UserUpdateDto;
import com.github.vincemann.springrapid.core.security.RapidRoles;
import com.github.vincemann.springrapid.core.controller.dto.mapper.context.Direction;
import com.github.vincemann.springrapid.core.controller.dto.mapper.context.DtoMappingContext;
import com.github.vincemann.springrapid.core.controller.dto.mapper.context.RapidDtoEndpoint;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(MyUserController.BASE_URI)
public class MyUserController extends AbstractUserController<User, Long>  {

    public static final String BASE_URI = "/api/core";


    @Override
    public DtoMappingContext provideDtoMappingContext() {
        return LemonDtoMappingContextBuilder.builder((DtoMappingContext) super.provideDtoMappingContext())
                .forEndpoint(properties.controller.endpoints.update, UserUpdateDto.class)
                .forEndpoint(lemonProperties.controller.endpoints.signup, Direction.REQUEST,MySignupForm.class)
                .withRoles(RapidRoles.ADMIN)
                .forEndpoint(properties.controller.endpoints.update, AdminUpdateUserDto.class)
                .build();
    }
}