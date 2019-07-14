package io.github.vincemann.generic.crud.lib.controller.dtoMapper.idResolver.uniDir.testEntities;

import io.github.vincemann.generic.crud.lib.model.IdentifiableEntityImpl;
import io.github.vincemann.generic.crud.lib.model.uniDir.UniDirChildEntity;
import io.github.vincemann.generic.crud.lib.model.uniDir.UniDirParent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UniDirEntityParent extends IdentifiableEntityImpl<Long> implements UniDirParent {
    @UniDirChildEntity
    private UniDirEntityParentsChild entityChild;
}
