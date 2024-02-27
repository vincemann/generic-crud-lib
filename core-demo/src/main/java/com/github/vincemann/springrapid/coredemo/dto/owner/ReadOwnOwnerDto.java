package com.github.vincemann.springrapid.coredemo.dto.owner;

import com.github.vincemann.springrapid.coredemo.dto.owner.abs.AbstractReadOwnerDto;
import com.github.vincemann.springrapid.coredemo.model.ClinicCard;
import com.github.vincemann.springrapid.autobidir.id.annotation.child.BiDirChildId;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class ReadOwnOwnerDto extends AbstractReadOwnerDto {

    private String dirtySecret;
    private String firstName;
    private String lastName;

    @BiDirChildId(ClinicCard.class)
    private Long clinicCardId;

    @Builder
    public ReadOwnOwnerDto(String address, String city, String telephone, Set<String> hobbies, Set<Long> petIds, String dirtySecret, String firstName, String lastName, Long clinicCardId) {
        super(address, city, telephone, hobbies, petIds);
        this.dirtySecret = dirtySecret;
        this.firstName = firstName;
        this.lastName = lastName;
        this.clinicCardId = clinicCardId;
    }
}
