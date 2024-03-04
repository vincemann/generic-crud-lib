package com.github.vincemann.springrapid.acldemo.controller.suite.templates;

import com.github.vincemann.springrapid.acldemo.controller.OwnerController;
import com.github.vincemann.springrapid.acldemo.dto.owner.ReadOwnOwnerDto;
import com.github.vincemann.springrapid.acldemo.dto.owner.SignupOwnerDto;
import com.github.vincemann.springrapid.authtest.AbstractUserControllerTestTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Component
public class OwnerControllerTestTemplate extends AbstractUserControllerTestTemplate<OwnerController> {

    public ReadOwnOwnerDto signup(SignupOwnerDto dto) throws Exception {
        String json = getMvc().perform(post(getController().getSignupUrl())
                        .content(serialize(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        return getController().getJsonMapper().readDto(json,ReadOwnOwnerDto.class);
    }

    public void addPetsSpectator(long permittedOwner, long targetOwner, String token) throws Exception {
        getMvc().perform(get(getController().getAddPetSpectatorUrl())
                .param("permitted",String.valueOf(permittedOwner))
                .param("target",String.valueOf(targetOwner)))
                .andExpect(status().is2xxSuccessful());
    }
}
