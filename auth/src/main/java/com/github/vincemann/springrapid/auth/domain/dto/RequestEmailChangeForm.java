package com.github.vincemann.springrapid.auth.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class RequestEmailChangeForm {
    // no pw bc token is used for authentication
//    @Password(groups = {UserVerifyUtils.ChangeEmailValidation.class})
//    private String password;
//    @UniqueEmail(/*groups = {UserVerifyUtils.ChangeEmailValidation.class}*/)
    @NotBlank
    @Email
    private String newEmail;
}
