/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.springrapid.log.nickvl.service;

import com.github.vincemann.springrapid.log.nickvl.annotation.LogInfo;
import com.github.vincemann.springrapid.log.nickvl.annotation.Lp;

/**
 * Base implementation of {@link BarService}.
 */
public abstract class AbstractBarService implements BarService {

    @LogInfo
    @Override
    public void inAbstract(String aFirst, String aSecond) {
        // do not override
    }

    @LogInfo
    @Override
    public void overridden(@Lp String aFirst, String aSecond) {
        // should be overridden
    }

    @LogInfo
    @Override
    public void overriddenLogInAbstractOnly(String aFirst, String aSecond) {
        // should be overridden
    }
}
