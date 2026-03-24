package org.example.spqr.rest.service;

import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.Event;
import org.example.spqr.rest.mappers.Domain2ResponseMapper;
import org.example.spqr.rest.mappers.Request2DomainMapper;
import org.example.spqr.models.rq.EventRq;
import org.example.spqr.models.rs.EventRs;
import org.example.spqr.sql.dao.ActivationDao;
import org.example.spqr.sql.dao.EventDao;
import org.example.spqr.sql.dao.EntityDao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component("eventService")
public class EventService {

    private final EntityDao entityDao;
    private final EventDao eventDao;
    private final ActivationDao activationDao;
    private final TransactionOperations transactionOperations;

    private final Request2DomainMapper request2DomainMapper;
    private final Domain2ResponseMapper domain2ResponseMapper;

    public EventService(
            EntityDao entityDao,
            EventDao eventDao,
            ActivationDao activationDao,
            TransactionOperations transactionOperations) {
        this.entityDao = entityDao;
        this.eventDao = eventDao;
        this.activationDao = activationDao;
        this.transactionOperations = transactionOperations;
        this.request2DomainMapper = new Request2DomainMapper();
        this.domain2ResponseMapper = new Domain2ResponseMapper();
    }

    public Map<String, EventRs> getLatestByEntities(List<String> entityIds, String shardName) {
        List<Entity> entities = entityDao.getByIds(entityIds);
        Map<String, Event> entityIds2Events = eventDao.getLatestByEntities(entities, shardName);
        return domain2ResponseMapper.from(entityIds2Events);
    }

    public Map<String, EventRs> addEvents(Map<String, EventRq> entity2Events) {
        List<Entity> entities = activationDao.getByEntityIds(new ArrayList<>(entity2Events.keySet()));
        Map<String, Event> result = transactionOperations.execute(status -> {
            Map<String, List<Event>> actualEvents = eventDao.getByEntities(entities);
            Map<String, Event> missingEvents = entity2Events.entrySet()
                    .stream()
                    .filter(e2e -> actualEvents.get(e2e.getKey()) == null || actualEvents.get(e2e.getKey())
                            .stream().noneMatch(ev -> Objects.equals(ev.eventName(), e2e.getValue().getEventName())))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> request2DomainMapper.from(entry.getValue(),
                                    entities.stream().filter(o -> Objects.equals(o.entityId(), entry.getKey())).findFirst().get())
                    ));
            missingEvents.values().forEach(eventDao::add);
            return missingEvents;
        });

        return domain2ResponseMapper.from(result);
    }
}
