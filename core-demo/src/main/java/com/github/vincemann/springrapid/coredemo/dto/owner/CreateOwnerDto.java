package com.github.vincemann.springrapid.coredemo.dto.owner;


import com.github.vincemann.springrapid.coredemo.model.ClinicCard;
import com.github.vincemann.springrapid.coredemo.model.Owner;
import com.github.vincemann.springrapid.coredemo.model.Pet;
import com.github.vincemann.springrapid.autobidir.id.annotation.child.BiDirChildId;
import com.github.vincemann.springrapid.autobidir.id.annotation.child.BiDirChildIdCollection;

import lombok.*;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter @Setter
public class CreateOwnerDto extends AbstractOwnerDto  {

    @NotBlank
    @Size(min = 2, max = 20)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 20)
    private String lastName;

    @BiDirChildIdCollection(Pet.class)
    private Set<Long> petIds = new HashSet<>();

    @Null
    @Override
    public Long getId() {
        return super.getId();
    }


    @BiDirChildId(ClinicCard.class)
    private Long clinicCardId;

    @Builder
    public CreateOwnerDto(@Size(min = 10, max = 255) @NotBlank String address, @NotBlank String city, @Size(min = 10, max = 10) String telephone, @NotBlank @Size(min = 2, max = 20) String firstName, @NotBlank @Size(min = 2, max = 20) String lastName,Set<String> hobbies, Long clinicCardId) {
        super(address, city, telephone, hobbies);
        this.clinicCardId =clinicCardId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public CreateOwnerDto(Owner owner){
        super(owner.getAddress(), owner.getCity(), owner.getTelephone(),owner.getHobbies());
        this.firstName = owner.getFirstName();
        this.lastName = owner.getLastName();
    }

    @NotBlank
    @Override
    public @Size(min = 10, max = 255) String getAddress() {
        return super.getAddress();
    }

    @NotBlank
    @Override
    public @Size(min=3,max=255) String getCity() {
        return super.getCity();
    }

    @Nullable
    @Override
    public @Size(min = 10, max = 10) String getTelephone() {
        return super.getTelephone();
    }
}
