package com.github.vincemann.springrapid.coredemo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.github.vincemann.springrapid.coredemo.model.LazyLoadedItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LazyLoadedItemRepository extends JpaRepository<LazyLoadedItem,Long> {
}
