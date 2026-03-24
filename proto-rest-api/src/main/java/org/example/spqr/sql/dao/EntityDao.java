package org.example.spqr.sql.dao;

import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.EntityChangeSet;
import org.joda.time.DateTime;

import java.util.List;

public interface EntityDao {

    Entity add(Entity entity);

    List<Entity> add(List<Entity> entities);

    Entity getById(String entityId);

    Entity getById(String externalId, int period);

    List<Entity> getPreconditionsByIds(List<String> entityIds);

    List<Entity> getEntities(int limit, int offset);

    List<Entity> getByIds(List<String> entityIds);

    boolean exists(String entityId);

    boolean update(Entity entity, EntityChangeSet changeset, DateTime originalUpdateTime);
}
