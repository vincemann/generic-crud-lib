package com.github.vincemann.springrapid.coredemo.dto.owner;

import com.github.vincemann.springrapid.coredemo.model.ClinicCard;
import com.github.vincemann.springrapid.autobidir.id.annotation.child.BiDirChildId;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@NoArgsConstructor
@Getter @Setter
public class ReadOwnOwnerDto extends ReadForeignOwnerDto {

    private String dirtySecret;

    @NotBlank
    @Size(min = 2, max = 20)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 20)
    private String lastName;

    @BiDirChildId(ClinicCard.class)
    private Long clinicCardId;

    @Builder(builderMethodName = "Builder")
    public ReadOwnOwnerDto(Set<Long> petIds, @Size(min = 10, max = 255) @NotBlank String address, @NotBlank String city, @Size(min = 10, max = 10) String telephone, String dirtySecret, @NotBlank @Size(min = 2, max = 20) String firstName, @NotBlank @Size(min = 2, max = 20) String lastName,Set<String> hobbies, Long clinicCardId) {
        super(petIds, address, city, telephone,hobbies);
        this.clinicCardId =clinicCardId;
        this.dirtySecret = dirtySecret;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
