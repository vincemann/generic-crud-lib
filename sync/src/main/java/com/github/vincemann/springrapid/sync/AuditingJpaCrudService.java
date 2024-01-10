package com.github.vincemann.springrapid.sync;

import com.github.vincemann.springrapid.core.IdConverter;
import com.github.vincemann.springrapid.core.model.AuditingEntity;
import com.github.vincemann.springrapid.core.repo.RapidJpaRepository;
import com.github.vincemann.springrapid.core.service.EntityFilter;
import com.github.vincemann.springrapid.core.service.JPACrudService;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import com.github.vincemann.springrapid.sync.dto.EntityLastUpdateInfo;
import com.github.vincemann.springrapid.sync.dto.EntitySyncStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AuditingJpaCrudService
        <
                E extends AuditingEntity<Id>,
                Id extends Serializable,
                R extends RapidJpaRepository<E, Id> & AuditingRepository<Id>>
        extends JPACrudService<E, Id, R>
        implements AuditingService<E, Id> {

    private IdConverter<Id> idConverter;

    @Autowired
    public void setIdConverter(IdConverter<Id> idConverter) {
        this.idConverter = idConverter;
    }

    @Transactional
    @Override
    public EntitySyncStatus findEntitySyncStatus(EntityLastUpdateInfo lastUpdateInfo) {
        String id = lastUpdateInfo.getId();
        Id convertedId = idConverter.toId(id);
        Date lastModified = getRepository().findLastModifiedDateByIdEquals(convertedId);
        char status;
        boolean updated;
        if (lastModified == null){
            assert getRepository().findById(convertedId).isEmpty();
            updated = true;
            status = 'r';
        }else{
            updated = lastUpdateInfo.getLastUpdate().after(lastModified);
            status = 'u';
        }
        if (updated) {
            return EntitySyncStatus.builder()
                    .id(id)
                    .status(status)
                    .build();
        } else {
            return null;
        }
    }

    @Override
    public Set<EntitySyncStatus> findEntitySyncStatusesSinceTimestamp(Timestamp lastUpdate, Set<EntityFilter<E>> filters) throws EntityNotFoundException {
        return null;
    }

    /**
     * only returns set of {@link EntitySyncStatus} for entities that need update.
     * @param lastUpdateInfos
     * @return
     */
    @Override
    public Set<EntitySyncStatus> findEntitySyncStatuses(Set<EntityLastUpdateInfo> lastUpdateInfos) {
        // todo speed this up maybe
        // maybe add parallel flag ?
        return lastUpdateInfos.stream()
                .map(this::findEntitySyncStatus)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
