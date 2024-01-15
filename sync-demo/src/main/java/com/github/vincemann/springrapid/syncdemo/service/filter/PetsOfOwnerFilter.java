package com.github.vincemann.springrapid.syncdemo.service.filter;

import com.github.vincemann.springrapid.core.service.filter.jpa.ParentFilter;
import com.github.vincemann.springrapid.core.slicing.ServiceComponent;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ServiceComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PetsOfOwnerFilter extends ParentFilter {

    public PetsOfOwnerFilter() {
        super("owner");
    }
}
