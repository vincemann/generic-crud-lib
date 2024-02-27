package com.github.vincemann.springrapid.syncdemo.dto;

import com.github.vincemann.springrapid.coredemo.dto.abs.IdAwareDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class PetTypeDto extends IdAwareDto {

    @NotEmpty
    @Size(min = 2, max = 20)
    private String name;

    @Builder
    public PetTypeDto(Long id, String name) {
        super(id);
        this.name = name;
    }

    @Override
    public String toString() {
        return "PetTypeDto{" +
                "name='" + name + '\'' +
                ", id=" + getId() +
                '}';
    }
}
