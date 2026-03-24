package org.example.spqr.sql.dao.impl;


import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.executor.BatchResult;
import org.example.spqr.models.dm.EntityChangeSetDm;
import org.example.spqr.models.dm.EntityDm;
import org.example.spqr.models.dm.EntitySearchSetDm;
import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.EntityChangeSet;
import org.example.spqr.sql.dao.EntityDao;
import org.example.spqr.sql.mappers.Data2DomainMapper;
import org.example.spqr.sql.mappers.Domain2DataMapper;
import org.example.spqr.sql.mappers.SqlExceptionMapper;
import org.example.spqr.sql.sqlmappers.MapperBounds;
import org.example.spqr.sql.sqlmappers.EntitySqlMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.partition;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.joda.time.DateTime.now;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;


@Component("entityDao")
@Transactional(propagation = REQUIRED)
public class SqlEntityDao implements EntityDao {

    private static final Logger LOG = getLogger(SqlEntityDao.class);
    public static final int MAX_IN_OPERATOR_ITEMS = 1_000;
    public static final Comparator<EntityDm> ENTITY_ASC_COMPARATOR = Comparator.comparing(EntityDm::getUpdatedAt, nullsFirst(naturalOrder()));

    @Autowired
    private SqlExceptionMapper sqlExceptionMapper;
    @Autowired
    private Domain2DataMapper domain2DataMapper;
    @Autowired
    private Data2DomainMapper data2DomainMapper;
    @Resource(name = "entitySqlMapper")
    private EntitySqlMapper entitySqlMapper;
    @Value("${database.maxStoragePeriod:365}")
    private Integer maxStoragePeriod;
    @Value("${database.transactionTimeout:60}")
    private Integer transactionTimeout;

    @Override
    public Entity add(Entity entity) {
        return add(singletonList(entity)).stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Entity> add(List<Entity> entities) {
        LOG.trace("Inserting following entities: {}", entities);
        try {
            List<EntityDm> entityDms = domain2DataMapper.from(toArray(entities, Entity.class));
            entityDms.forEach(entitySqlMapper::insert);
            entitySqlMapper.flush();
            LOG.trace("Entites were successfully inserted: {}", entityDms);
            return entities;
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Entity getById(String entityId) {
        EntitySearchSetDm entitySearchSetDm = EntitySearchSetDm.builder()
                .entityId(entityId)
                .startSearchTime(getMaxStorageDate())
                .endSearchTime(getCurrentTimeWithMaxDifference())
                .build();
        EntityDm entityDm;
        try {
            List<EntityDm> entityDmList = entitySqlMapper.selectByIds(entitySearchSetDm);
            entityDm = getMostProgressive(entityDmList);
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
        return data2DomainMapper.from(entityDm);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Entity getById(String entityId, int period) {
        DateTime minDate = now().dayOfMonth().roundFloorCopy().minusDays(period);
        EntityDm entityDm;
        EntitySearchSetDm searchSetDm = EntitySearchSetDm.builder()
                .entityId(entityId)
                .startSearchTime(minDate)
                .endSearchTime(getCurrentTimeWithMaxDifference())
                .build();

        try {
            List<EntityDm> entityDmList = entitySqlMapper.selectByIds(searchSetDm);
            entityDm = getMostProgressive(entityDmList);
            LOG.trace("Selected entity {} by id = '{}' and createdAt >= '{}'", entityDm, entityId, minDate);
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }

        return data2DomainMapper.from(entityDm);
    }

    @Override
    public List<Entity> getPreconditionsByIds(List<String> externalIds) {
        if (CollectionUtils.isEmpty(externalIds)) {
            return emptyList();
        }

        final List<Entity> preconditionEntities;
        EntitySearchSetDm.Builder searchSetBuilder = EntitySearchSetDm.builder()
                .startSearchTime(getMaxStorageDate())
                .endSearchTime(getCurrentTimeWithMaxDifference());
        try {
            preconditionEntities = partition(externalIds, MAX_IN_OPERATOR_ITEMS).stream()
                    .flatMap(chunk -> {
                        EntitySearchSetDm entitySearchSetDm = searchSetBuilder.entityIds(chunk).build();
                        return entitySqlMapper.selectByIds(entitySearchSetDm)
                                .stream()
                                .collect(collectingAndThen(
                                        groupingBy(EntityDm::getEntityId, collectingAndThen(toList(), SqlEntityDao::getMostProgressive)),
                                        map -> map.values().stream()))
                                .map(o -> (EntityDm) o)
                                .map(data2DomainMapper::from);
                    })
                    .collect(toList());
            LOG.trace("Selected precondition entities: {}", preconditionEntities);
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
        return preconditionEntities;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<Entity> getEntities(int limit, int offset) {
        LOG.trace("Selecting entities with criteria limit = '{}' and offset = '{}'", limit, offset);
        MapperBounds mapperBounds = new MapperBounds(limit, offset);
        EntitySearchSetDm searchSetDm = EntitySearchSetDm.builder()
                .startSearchTime(getMaxStorageDate())
                .endSearchTime(getCurrentTimeWithMaxDifference())
                .build();
        try {
            List<EntityDm> entityDmList = entitySqlMapper.selectLastUpdated(mapperBounds, searchSetDm);
            List<Entity> entityList = data2DomainMapper.from(toArray(entityDmList, EntityDm.class));
            LOG.trace("Selected entities: {}", entityList);
            return entityList;
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }

    @Override
    public List<Entity> getByIds(List<String> entityIds) {
        if (isEmpty(entityIds)) {
            return emptyList();
        }
        LOG.trace("Selecting entities by ids: {}", entityIds);

        EntitySearchSetDm.Builder searchSetBuilder = EntitySearchSetDm.builder()
                .startSearchTime(getMaxStorageDate())
                .endSearchTime(getCurrentTimeWithMaxDifference());
        final List<Entity> entities;
        try {
            entities = partition(entityIds, MAX_IN_OPERATOR_ITEMS).stream()
                    .flatMap(chunk -> {
                        EntitySearchSetDm entitySearchSetDm = searchSetBuilder.entityIds(chunk).build();
                        return entitySqlMapper.selectByIds(entitySearchSetDm)
                                .stream()
                                .collect(collectingAndThen(
                                        groupingBy(EntityDm::getEntityId, collectingAndThen(toList(), SqlEntityDao::getMostProgressive)),
                                        map -> map.values().stream()))
                                .map(data2DomainMapper::from);
                    })
                    .collect(toList());
            LOG.trace("Selected entities: {}", entities);
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
        return entities;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public boolean exists(String entityId) {
        DateTime currentTime = now();
        EntitySearchSetDm searchSetDm = EntitySearchSetDm.builder()
                .entityId(entityId)
                .startSearchTime(currentTime.withTimeAtStartOfDay().minusDays(maxStoragePeriod))
                .endSearchTime(getCurrentTimeWithMaxDifference())
                .build();
        try {
            return entitySqlMapper.isEntityExists(searchSetDm);
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }

    @Override
    public boolean update(Entity entity, EntityChangeSet changeSet, DateTime originalUpdateTime) {
        LOG.trace("Trying to safely update entity '{}' with changeset: {}", entity.entityId(), changeSet);
        ZonedDateTime createdAt = entity.status().createdAt();
        EntitySearchSetDm searchSetDm = EntitySearchSetDm.builder()
                .entityId(entity.entityId())
                .creationTime(createdAt)
                .updateTime(originalUpdateTime)
                .build();
        EntityChangeSetDm changeSetDm = domain2DataMapper.from(changeSet);
        try {
            entitySqlMapper.update(searchSetDm, changeSetDm);
            List<BatchResult> updateBatchResults = entitySqlMapper.flush();
            List<Integer> updateResults = getUpdateCountsFromBatchResults(updateBatchResults);
            if (updateResults.isEmpty() || updateResults.get(0) <= 0) {
                return false;
            }
            LOG.trace("Entity '{}' was safely updated with changeset: {}", entity.entityId(), changeSet);
            return true;
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }

    private DateTime getMaxStorageDate() {
        DateTime currentDate = now();
        return currentDate.withTimeAtStartOfDay().minusDays(maxStoragePeriod);
    }

    private DateTime getCurrentTimeWithMaxDifference() {
        DateTime currentDate = now();
        return currentDate.plusSeconds(600);
    }

    private List<Integer> getUpdateCountsFromBatchResults(List<BatchResult> batchResults) {
        return batchResults.stream().flatMap(batch -> Arrays.stream(batch.getUpdateCounts()).boxed()).collect(toList());
    }

    public static <T extends EntityDm> T getMostProgressive(List<T> entities) {
        if (isEmpty(entities)) {
            return null;
        }

        if (entities.size() == 1) {
            return entities.getFirst();
        }

        final T entityDm = Collections.max(entities, ENTITY_ASC_COMPARATOR);
        LOG.debug("Database contains several entities with id: {}", entityDm.getEntityId());
        return entityDm;
    }
}
