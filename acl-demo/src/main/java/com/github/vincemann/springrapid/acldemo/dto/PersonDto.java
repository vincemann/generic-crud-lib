package com.github.vincemann.springrapid.acldemo.dto;


import com.github.vincemann.springrapid.acldemo.dto.abs.MyIdDto;
import com.github.vincemann.springrapid.acldemo.model.abs.MyIdentifiableEntity;
import com.github.vincemann.springrapid.core.model.IdentifiableEntityImpl;
import lombok.*;

import javax.validation.constraints.Size;


@Getter
@Setter
@NoArgsConstructor

@AllArgsConstructor
public class PersonDto extends MyIdDto<Long> {
    @Size(min = 2, max = 20)
    private String firstName;

    @Size(min = 2, max = 20)
    private String lastName;
}
