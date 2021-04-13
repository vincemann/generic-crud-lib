package com.github.vincemann.springrapid.coredemo.dtos.owner;

import com.github.vincemann.springrapid.coredemo.model.Pet;
import com.github.vincemann.springrapid.entityrelationship.model.child.annotation.BiDirChildCollection;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class ReadOwnOwnerDto extends ReadForeignOwnerDto {
    public static final String DIRTY_SECRET = "can you see this?";
    private String dirtySecret = DIRTY_SECRET;
    @NotBlank
    @Size(min = 2, max = 20)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 20)
    private String lastName;

    @Builder(builderMethodName = "Builder")
    public ReadOwnOwnerDto(Set<Long> petIds, @Size(min = 10, max = 255) @NotBlank String address, @NotBlank String city, @Size(min = 10, max = 10) String telephone, String dirtySecret, @NotBlank @Size(min = 2, max = 20) String firstName, @NotBlank @Size(min = 2, max = 20) String lastName) {
        super(petIds, address, city, telephone);
        this.dirtySecret = dirtySecret;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
