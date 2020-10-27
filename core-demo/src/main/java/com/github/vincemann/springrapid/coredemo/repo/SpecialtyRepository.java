package com.github.vincemann.springrapid.coredemo.repo;

import com.github.vincemann.springrapid.core.slicing.components.ServiceComponent;
import com.github.vincemann.springrapid.coredemo.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ServiceComponent
public interface SpecialtyRepository extends JpaRepository<Specialty,Long> {
}
