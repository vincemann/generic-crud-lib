package com.github.vincemann.springrapid.syncdemo.repo;


import com.github.vincemann.springrapid.core.repo.RapidJpaRepository;
import com.github.vincemann.springrapid.syncdemo.model.ClinicCard;
import org.springframework.data.jpa.repository.JpaRepository;

//@DisableAutoBiDir
public interface ClinicCardRepository extends RapidJpaRepository<ClinicCard,Long> {
}