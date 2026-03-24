package org.example.spqr.sql.dao.impl;

import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.example.spqr.models.dm.EventDm;
import org.example.spqr.models.dm.EntitySearchSetDm;
import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.Event;
import org.example.spqr.sql.dao.EventDao;
import org.example.spqr.sql.mappers.Data2DomainMapper;
import org.example.spqr.sql.mappers.Domain2DataMapper;
import org.example.spqr.sql.mappers.SqlExceptionMapper;
import org.example.spqr.sql.sqlmappers.EventSqlMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.partition;
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.example.spqr.sql.dao.impl.SqlActivationDao.TIME_ASC_COMPARATOR;
import static org.example.spqr.sql.dao.impl.SqlEntityDao.MAX_IN_OPERATOR_ITEMS;
import static org.example.spqr.utils.DateUtils.toDateOrNull;
import static org.example.spqr.utils.DateUtils.toDateTime;
import static org.slf4j.LoggerFactory.getLogger;

@Component("eventDao")
@Transactional
public class SqlEventDao implements EventDao {

    private static final Logger LOG = getLogger(SqlEventDao.class);

    static final int BATCH_SIZE_FOR_EVENTS = 1000;

    @Autowired
    private SqlExceptionMapper sqlExceptionMapper;

    @Autowired
    private Domain2DataMapper domain2DataMapper;

    @Autowired
    private Data2DomainMapper data2DomainMapper;

    @Resource(name = "eventSqlMapper")
    private EventSqlMapper eventSqlMapper;

    @Override
    public Map<Entity, Event> add(Map<Entity, Event> eventMap) {
        if (isEmpty(eventMap)) {
            return emptyMap();
        }

        LOG.trace("Adding events to the DB: {}", eventMap);
        try {
            List<EventDm> eventDms = domain2DataMapper.from(eventMap);
            Lists.partition(eventDms, BATCH_SIZE_FOR_EVENTS)
                    .forEach(eventsBatch -> {
                        eventsBatch.forEach(event -> eventSqlMapper.insert(event));
                        eventSqlMapper.flush();
                    });

            LOG.debug("Added events: {}", eventMap);
            return eventMap;
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }

    @Override
    public Event add(Event event) {
        LOG.trace("Adding events to the DB: {}", event);
        try {
            EventDm eventDm = domain2DataMapper.from(event);
            eventSqlMapper.insert(eventDm);
            eventSqlMapper.flush();
            LOG.debug("Added events: {}", event);
            return event;
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }

    @Override
    public Map<String, List<Event>> getByEntities(List<Entity> entities) {
        if (isEmpty(entities)) {
            return emptyMap();
        }

        LOG.trace("Selecting all events for entities: {}", entities);
        try {
            List<Entity> sortedEntities = entities.stream()
                    .sorted(TIME_ASC_COMPARATOR)
                    .collect(toList());
            Map<Pair<String, Date>, EventDm[]> eventDms = partition(sortedEntities, MAX_IN_OPERATOR_ITEMS).stream()
                    .flatMap(chunk -> {
                        List<String> entityIds = chunk.stream()
                                .map(Entity::entityId)
                                .collect(toList());
                        DateTime createdAtFrom = toDateTime(chunk.get(0).status().createdAt());
                        DateTime createdAtTo = toDateTime(getLast(chunk).status().createdAt());

                        EntitySearchSetDm searchSetDm = EntitySearchSetDm.builder()
                                .entityIds(entityIds)
                                .startSearchTime(createdAtFrom)
                                .endSearchTime(createdAtTo)
                                .build();

                        return eventSqlMapper.selectByEntitiesSearchSet(searchSetDm).stream();

                    }).collect(groupingBy(event -> Pair.of(event.getEntityId(), event.getEntityCreatedAt()),
                            collectingAndThen(toList(), list -> list.toArray(new EventDm[0]))));

            LOG.debug("Selected events: {}", eventDms);
            return entities.stream()
                    .collect(LinkedHashMap::new, (map, entity) -> map.put(entity.entityId(),
                            data2DomainMapper.from(eventDms.get(Pair.of(entity.entityId(), toDateOrNull(entity.status().createdAt()))))), Map::putAll);
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }

    @Override
    public Map<String, Event> getLatestByEntities(List<Entity> entities, String shardName) {
        if (isEmpty(entities)) {
            return emptyMap();
        }
        String shardHint = shardName != null ? "__spqr__execute_on: " + shardName : "";
        LOG.trace("Selecting latest events for entities: {}", entities);
        try {
            List<Entity> sortedEntities = entities.stream()
                    .sorted(TIME_ASC_COMPARATOR)
                    .collect(toList());
            Map<String, EventDm> eventDms = partition(sortedEntities, MAX_IN_OPERATOR_ITEMS).stream()
                    .flatMap(chunk -> {
                        List<String> entityIds = chunk.stream().filter(Objects::nonNull)
                                .map(Entity::entityId)
                                .collect(toList());
                        DateTime createdAtFrom = toDateTime(chunk.get(0).status().createdAt());
                        DateTime createdAtTo = toDateTime(getLast(chunk).status().createdAt());

                        EntitySearchSetDm searchSetDm = EntitySearchSetDm.builder()
                                .entityIds(entityIds)
                                .startSearchTime(createdAtFrom)
                                .endSearchTime(createdAtTo)
                                .build();

                        return eventSqlMapper.selectLatestByEntities(searchSetDm, shardHint).stream();

                    }).collect(toMap(EventDm::getEntityId, identity(), (e1, e2) -> e1));

            LOG.debug("Selected events: {}", eventDms);
            return entities.stream()
                    .collect(LinkedHashMap::new, (map, entity) -> map.put(entity.entityId(),
                            data2DomainMapper.from(eventDms.get(entity.entityId()))), Map::putAll);
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }
}
