package com.github.vincemann.springrapid.coredemo.model;

import com.github.vincemann.springrapid.core.model.IdentifiableEntityImpl;

import com.github.vincemann.springrapid.autobidir.model.parent.annotation.BiDirParentEntity;
import com.github.vincemann.springrapid.coredemo.model.abs.MyIdentifiableEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "clinic_cards")
public class ClinicCard extends MyIdentifiableEntity<Long> {


    @BiDirParentEntity
    @OneToOne(mappedBy = "clinicCard")
    private Owner owner;
    private Date registrationDate;
    private String registrationReason;

    @Builder
    public ClinicCard(Owner owner, Date registrationDate, String registrationReason) {
        this.owner = owner;
        this.registrationDate = registrationDate;
        this.registrationReason = registrationReason;
    }

    @Override
    public String toString() {
        return "test";
//        return "ClinicCard{" +
//                "registrationDate=" + registrationDate +
//                ", registrationReason='" + registrationReason + '\'' +
//                '}';
    }
}
