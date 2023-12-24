package com.github.vincemann.springrapid.auth.service.context;

import com.github.vincemann.springrapid.auth.model.AbstractUser;
import com.github.vincemann.springrapid.auth.util.UserUtils;
import com.github.vincemann.springrapid.core.service.context.ServiceCallContext;
import com.github.vincemann.springrapid.core.service.context.ThrowingSupplier;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

public class AuthServiceCallContext extends ServiceCallContext {

    private UserUtils userUtils;

    @Autowired
    public void setUserUtils(UserUtils userUtils) {
        this.userUtils = userUtils;
    }

    public <U extends AbstractUser> U resolveAuthenticated(){
        return getCached("authenticated",() -> userUtils.findAuthenticatedUser());
    }

    public <U extends AbstractUser> U resolveUser(Long id) throws EntityNotFoundException {
        return getThrowingCached(computeUserKey(id), new ThrowingSupplier<U, EntityNotFoundException>() {
            @Override
            public U get() throws EntityNotFoundException {
                return userUtils.findUserById(id);
            }
        });
    }

    private String computeUserKey(Serializable id){
        return "User:" + String.valueOf(id);
    }
}
