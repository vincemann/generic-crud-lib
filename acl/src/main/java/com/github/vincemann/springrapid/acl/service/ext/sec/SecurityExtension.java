package com.github.vincemann.springrapid.acl.service.ext.sec;

import com.github.vincemann.springrapid.acl.AclTemplate;
import com.github.vincemann.springrapid.core.proxy.BasicServiceExtension;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Write extensions for secured Services
 * @see com.github.vincemann.springrapid.core.proxy.BasicServiceExtension
 */
@Getter
public abstract class SecurityExtension<T>
        extends BasicServiceExtension<T> {

    protected AclTemplate securityChecker;

    @Autowired
    public void setAclSecurityChecker(AclTemplate securityChecker) {
        this.securityChecker = securityChecker;
    }
}