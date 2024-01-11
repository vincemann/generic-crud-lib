package com.github.vincemann.springrapid.sync.service;

import com.github.vincemann.springrapid.core.model.AuditingEntity;
import com.github.vincemann.springrapid.core.service.EntityFilter;
import com.github.vincemann.springrapid.core.service.JPQLEntityFilter;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import com.github.vincemann.springrapid.sync.model.EntityLastUpdateInfo;
import com.github.vincemann.springrapid.sync.model.EntitySyncStatus;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface SyncService<E extends AuditingEntity<Id>,Id extends Serializable> {

    EntitySyncStatus findEntitySyncStatus(EntityLastUpdateInfo lastUpdateInfo);

    Set<EntitySyncStatus> findEntitySyncStatusesSinceTimestamp(Timestamp lastUpdate, List<JPQLEntityFilter<E>> jpqlFilters);


    Set<EntitySyncStatus> findEntitySyncStatuses(Collection<EntityLastUpdateInfo> lastUpdateInfos);
}
