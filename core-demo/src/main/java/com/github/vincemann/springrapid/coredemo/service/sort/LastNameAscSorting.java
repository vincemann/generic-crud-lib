package com.github.vincemann.springrapid.coredemo.service.sort;

import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.service.filter.jpa.SortingExtension;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.SortDefinition;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;

@Component
public class LastNameAscSorting implements SortingExtension {

    @Override
    public String getName() {
        return "name-asc";
    }

    @Override
    public void setArgs(String... args) throws BadEntityException {

    }

    @Override
    public SortDefinition getSort() {
        return new MutableSortDefinition("lastName",false,true);
    }

    @Override
    public String toString() {
        return "LastNameAscSorting{}";
    }
}