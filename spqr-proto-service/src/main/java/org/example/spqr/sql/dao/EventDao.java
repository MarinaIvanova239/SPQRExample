package org.example.spqr.sql.dao;

import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.Event;

import java.util.List;
import java.util.Map;

public interface EventDao {

    Map<Entity, Event> add(Map<Entity, Event> eventMap);

    Event add(Event event);

    Map<String, List<Event>> getByEntities(List<Entity> entities);

    Map<String, Event> getLatestByEntities(List<Entity> entities, String shardName);
}
