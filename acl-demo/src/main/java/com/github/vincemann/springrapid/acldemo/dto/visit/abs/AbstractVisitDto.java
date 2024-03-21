package com.github.vincemann.springrapid.acldemo.dto.visit.abs;

import com.github.vincemann.springrapid.acldemo.model.Owner;
import com.github.vincemann.springrapid.acldemo.model.Pet;
import com.github.vincemann.springrapid.acldemo.model.Vet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
public class AbstractVisitDto {

    private Set<Long> petIds = new HashSet<>();
    private Long ownerId;
    private Long vetId;
    private LocalDate date;
    private String reason;

    public AbstractVisitDto(Set<Long> petIds, Long ownerId, Long vetId, LocalDate date, String reason) {
        this.petIds = petIds;
        this.ownerId = ownerId;
        this.vetId = vetId;
        this.date = date;
        this.reason = reason;
    }
}
