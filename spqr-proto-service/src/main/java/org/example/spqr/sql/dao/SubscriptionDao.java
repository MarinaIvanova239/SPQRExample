package org.example.spqr.sql.dao;


import org.example.spqr.models.domain.Subscription;

import java.time.ZonedDateTime;
import java.util.List;

public interface SubscriptionDao {

    int addSubscriptions(List<Subscription> subscriptions);

    List<Subscription> getSubscriptions(String entityId, ZonedDateTime entityCreatedAt);
}
