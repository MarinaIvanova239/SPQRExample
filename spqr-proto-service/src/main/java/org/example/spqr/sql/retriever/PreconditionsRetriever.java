package org.example.spqr.sql.retriever;

import org.example.spqr.models.domain.Entity;
import org.example.spqr.sql.dao.EntityDao;

import java.util.List;

import static com.google.common.collect.Iterables.isEmpty;

public record PreconditionsRetriever(EntityDao entityDao) {

    public Entity getPrecondition(String entityId) {
        List<Entity> preconditionEntities = entityDao.getPreconditionsByIds(List.of(entityId));
        if (preconditionEntities == null || isEmpty(preconditionEntities)) {
            return null;
        }
        return preconditionEntities.getFirst();
    }
}
