package com.github.vincemann.springrapid.acldemo.model.abs;

import com.github.vincemann.springrapid.acldemo.model.User;
import com.github.vincemann.springrapid.core.model.IdentifiableEntity;

public interface UserAwareEntity extends IdentifiableEntity<Long> {
    User getUser();
    void setUser(User user);

}