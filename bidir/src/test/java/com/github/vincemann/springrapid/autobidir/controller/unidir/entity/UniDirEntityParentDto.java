package com.github.vincemann.springrapid.autobidir.controller.unidir.entity;

import com.github.vincemann.springrapid.autobidir.resolveid.annotation.child.UniDirChildId;

import com.github.vincemann.springrapid.core.model.IdentifiableEntityImpl;



public class UniDirEntityParentDto extends IdentifiableEntityImpl<Long>  {
    @UniDirChildId(UniDirEntityParentsChild.class)
    private Long childId;

    public UniDirEntityParentDto() {
    }

    public Long getChildId() {
        return childId;
    }

    public void setChildId(Long childId) {
        this.childId = childId;
    }
}