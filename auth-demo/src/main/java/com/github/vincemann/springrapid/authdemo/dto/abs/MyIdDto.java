package com.github.vincemann.springrapid.authdemo.dto.abs;

import com.github.vincemann.smartlogger.SmartLogger;
import com.github.vincemann.springrapid.core.dto.AbstractIdDto;

import java.io.Serializable;

public class MyIdDto<Id extends Serializable> extends AbstractIdDto<Id> {

    @Override
    public String toString() {
        return SmartLogger.builder()
                .build()
                .toString(this);
    }
}
