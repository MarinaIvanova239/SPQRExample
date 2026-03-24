package org.example.spqr.sql.dao;


import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.Request;
import org.example.spqr.models.domain.SubscriberInfo;

import java.util.List;

public interface ActivationDao {

    List<Entity> getSuspendedEntities(int maxCount, int minAge, int maxAge);

    Entity getByRequestInAnyState(Request request);

    List<Entity> getByEntityIds(List<String> entityIds);

    List<Entity> getSubscribed(List<SubscriberInfo> subscriberInfos);

    int updateActivationTime(List<Entity> entities);
}
