package com.github.vincemann.springrapid.syncdemo.dto.owner;

import com.github.vincemann.springrapid.syncdemo.dto.abs.MyIdDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractOwnerDto extends MyIdDto<Long> {

    public AbstractOwnerDto(@Size(min = 10, max = 255) @NotBlank String address, @NotBlank String city, @Nullable @Size(min = 10, max = 10) String telephone,Set<String> hobbies) {
        this.address = address;
        this.city = city;
        this.telephone = telephone;
        if (hobbies!=null)
            this.hobbies=hobbies;
    }

    @Size(min=10,max=255)
    private String address;

    @Size(min=3,max=255)
    protected String city;

    @Size(min=10,max=10)
    private String telephone;

    private Set<String> hobbies = new HashSet<>();


}
