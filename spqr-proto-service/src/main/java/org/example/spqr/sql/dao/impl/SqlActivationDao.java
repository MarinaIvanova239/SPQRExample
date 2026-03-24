package org.example.spqr.sql.dao.impl;


import jakarta.annotation.Resource;
import lombok.NonNull;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.executor.BatchResult;
import org.example.spqr.models.dm.EntityDm;
import org.example.spqr.models.dm.EntitySearchSetDm;
import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.EntityStatus;
import org.example.spqr.models.domain.Request;
import org.example.spqr.models.domain.SubscriberInfo;
import org.example.spqr.sql.dao.ActivationDao;
import org.example.spqr.sql.mappers.Data2DomainMapper;
import org.example.spqr.sql.mappers.Domain2DataMapper;
import org.example.spqr.sql.mappers.SqlExceptionMapper;
import org.example.spqr.sql.sqlmappers.ActivationSqlMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.partition;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.example.spqr.models.domain.EntityState.NEW;
import static org.example.spqr.models.domain.EntityState.SUSPENDED;
import static org.example.spqr.sql.dao.impl.SqlEntityDao.MAX_IN_OPERATOR_ITEMS;
import static org.example.spqr.sql.dao.impl.SqlEntityDao.getMostProgressive;
import static org.example.spqr.utils.DateUtils.toDateTime;
import static org.joda.time.DateTime.now;
import static org.slf4j.LoggerFactory.getLogger;

@Component("activationDao")
public class SqlActivationDao implements ActivationDao {

    static final Comparator<Entity> TIME_ASC_COMPARATOR = comparing(Entity::status, comparing(EntityStatus::createdAt));
    private static final Logger LOG = getLogger(SqlActivationDao.class);

    @Resource(name = "activationSqlMapper")
    private ActivationSqlMapper activationSqlMapper;
    @Autowired
    private SqlExceptionMapper sqlExceptionMapper;
    @Autowired
    private Domain2DataMapper domain2DataMapper;
    @Autowired
    private Data2DomainMapper data2DomainMapper;

    @Value("${database.transactionTimeout:60}")
    private Integer transactionTimeout;

    @Value("${database.maxStoragePeriod:365}")
    private Integer maxStoragePeriod;

    @Override
    public List<Entity> getSuspendedEntities(int maxCount, int minAge, int maxAge) {
        final DateTime currentDate = now();
        EntitySearchSetDm entitySearchSetDm = EntitySearchSetDm.builder()
                .startSearchTime(currentDate.minusDays(minAge))
                .endSearchTime(currentDate.minusDays(maxAge))
                .build();
        LOG.trace("Selecting suspended by condition: currentDate = '{}', maxCount = '{}'.", currentDate.toDate(), maxCount);
        try {
            List<EntityDm> suspendedEntityDms = activationSqlMapper.selectEntitiesWithExpiredActivatedAt(
                    SUSPENDED.value(), currentDate.toDate(), entitySearchSetDm, maxCount
            );
            List<Entity> sleepEntities = suspendedEntityDms.stream()
                    .map(data2DomainMapper::from)
                    .collect(toList());
            LOG.trace("Selected suspended: {}", sleepEntities);
            return sleepEntities;
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }

    @Override
    public Entity getByRequestInAnyState(@NonNull Request request) {
        final String entityId = request.entityId();
        final DateTime createdAt = toDateTime(request.entityCreatedAt());
        EntitySearchSetDm entitySearchSetDm = EntitySearchSetDm.builder()
                .entityId(entityId)
                .startSearchTime(createdAt)
                .endSearchTime(createdAt)
                .build();
        try {
            List<EntityDm> entityDms = activationSqlMapper.selectBySearchSet(entitySearchSetDm);
            EntityDm entityDm = getMostProgressive(entityDms);
            LOG.trace("Selected entity by id = '{}' and creationTime between '{}' and '{}': {}", entityId,
                    entitySearchSetDm.getStartSearchTime(), entitySearchSetDm.getEndSearchTime(), entityDm);
            return data2DomainMapper.from(entityDm);
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }

    @Override
    public List<Entity> getByEntityIds(List<String> entityIds) {
        if (isEmpty(entityIds)) {
            return emptyList();
        }
        LOG.trace("Select entities by ids: '{}'", entityIds);

        final DateTime currentTime = now();
        EntitySearchSetDm.Builder searchSetBuilder = EntitySearchSetDm.builder()
                .startSearchTime(currentTime.withTimeAtStartOfDay().minusDays(maxStoragePeriod))
                .endSearchTime(currentTime.plusSeconds(600));
        try {
            List<Entity> entities = partition(entityIds, MAX_IN_OPERATOR_ITEMS).stream()
                    .flatMap(chunk -> {
                        EntitySearchSetDm entitySearchSetDm = searchSetBuilder.entityIds(chunk).build();
                        return activationSqlMapper.selectBySearchSet(entitySearchSetDm)
                                .stream()
                                .map(e -> data2DomainMapper.from(e))
                                .toList()
                                .stream();
                    })
                    .collect(toList());
            LOG.trace("Selected entities: '{}'", entities);
            return entities;
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }

    @Override
    public List<Entity> getSubscribed(@NonNull List<SubscriberInfo> subscriberInfos) {
        if (isEmpty(subscriberInfos)) {
            return emptyList();
        }

        Map<String, List<EntityDm>> entitiesWithDuplicates = subscriberInfos.stream()
                .map(subscriberInfo -> {
                    String entityId = subscriberInfo.entityId();
                    DateTime createdAt = toDateTime(subscriberInfo.entityCreatedAt());
                    EntitySearchSetDm entitySearchSetDm = EntitySearchSetDm.builder()
                            .entityId(entityId)
                            .startSearchTime(createdAt)
                            .endSearchTime(createdAt)
                            .build();
                    try {
                        List<EntityDm> activationEntities = activationSqlMapper.selectBySearchSetAndStateId(
                                entitySearchSetDm, NEW.value());
                        return Pair.of(entityId, activationEntities);
                    } catch (Exception e) {
                        throw sqlExceptionMapper.toProtoException(e);
                    }
                })
                .collect(toMap(Pair::getLeft, Pair::getRight, ListUtils::union));

        return entitiesWithDuplicates.values().stream()
                .filter(entityDms -> !isEmpty(entityDms))
                .flatMap(List::stream)
                .map(e -> data2DomainMapper.from(e))
                .collect(toList());
    }

    @Override
    public int updateActivationTime(List<Entity> entities) {
        if (entities.isEmpty()) {
            return 0;
        }

        LOG.trace("Trying to touch {} entities safely...", entities.size());
        try {
            entities.forEach(entity -> {
                ZonedDateTime entityCreatedAt = entity.status().createdAt();
                EntitySearchSetDm entitySearchSetDm = EntitySearchSetDm.builder()
                        .entityId(entity.entityId())
                        .creationTime(entityCreatedAt)
                        .build();
                EntityDm entityDm = domain2DataMapper.from(entity);
                activationSqlMapper.updateActivatedAt(entityDm, entitySearchSetDm);
            });
            int updatedCount = getUpdatedCount(activationSqlMapper.flush());
            LOG.trace("{} entities were touched.", updatedCount);
            return updatedCount;
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }

    private int getUpdatedCount(List<BatchResult> batchResults) {
        return batchResults.stream()
                .flatMap(batch -> Arrays.stream(batch.getUpdateCounts()).boxed())
                .reduce(Integer::sum)
                .orElse(0);
    }
}
