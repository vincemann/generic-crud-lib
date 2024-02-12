package com.github.vincemann.springrapid.authdemo.dto;

import com.github.vincemann.springrapid.auth.dto.SignupDto;

import com.github.vincemann.springrapid.authdemo.service.ValidUsername;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class MySignupDto extends SignupDto {

    @ValidUsername
    @NotBlank(message = "{blank.name}")
    private String name;

    @NotBlank
    @Email
    @Override
    public String getContactInformation() {
        return super.getContactInformation();
    }



    @Builder
    public MySignupDto(String contactInformation, String password, String name) {
        super(contactInformation, password);
        this.name = name;
    }
}
