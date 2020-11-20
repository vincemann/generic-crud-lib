package com.github.vincemann.springrapid.coredemo.repo;

import com.github.vincemann.springrapid.core.slicing.ServiceComponent;
import com.github.vincemann.springrapid.coredemo.model.Vet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ServiceComponent
public interface VetRepository extends JpaRepository<Vet,Long> {
}
