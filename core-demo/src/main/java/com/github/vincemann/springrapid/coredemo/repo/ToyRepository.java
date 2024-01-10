package com.github.vincemann.springrapid.coredemo.repo;

import com.github.vincemann.springrapid.core.repo.RapidJpaRepository;
import com.github.vincemann.springrapid.coredemo.model.Toy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ToyRepository extends RapidJpaRepository<Toy,Long> {
    Optional<Toy> findByName(String name);
}
