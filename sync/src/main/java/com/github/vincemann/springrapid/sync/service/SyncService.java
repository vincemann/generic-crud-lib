package com.github.vincemann.springrapid.sync.service;

import com.github.vincemann.aoplog.api.AopLoggable;
import com.github.vincemann.aoplog.api.annotation.LogInteraction;
import com.github.vincemann.springrapid.core.model.AuditingEntity;
import com.github.vincemann.springrapid.core.model.IAuditingEntity;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import com.github.vincemann.springrapid.core.service.filter.EntityFilter;
import com.github.vincemann.springrapid.core.service.filter.jpa.QueryFilter;
import com.github.vincemann.springrapid.sync.model.EntityUpdateInfo;
import com.github.vincemann.springrapid.sync.model.EntitySyncStatus;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface SyncService<E extends IAuditingEntity<Id>,Id extends Serializable> extends AopLoggable {


    @LogInteraction
    @Transactional
    EntitySyncStatus findEntitySyncStatus(LastFetchInfo clientLastUpdate) throws EntityNotFoundException;

    @LogInteraction
    @Transactional
    Set<EntitySyncStatus> findEntitySyncStatusesSinceTimestamp(Timestamp lastUpdate, List<QueryFilter<? super E>> jpqlFilters);


    @LogInteraction
    @Transactional
    Set<EntitySyncStatus> findEntitySyncStatusesSinceTimestamp(Timestamp lastClientFetch, List<QueryFilter<? super E>> jpqlFilters, List<EntityFilter<? super E>> entityFilters);

    @LogInteraction
    @Transactional
    Set<EntitySyncStatus> findEntitySyncStatuses(Collection<LastFetchInfo> lastFetchInfo) throws EntityNotFoundException;
}