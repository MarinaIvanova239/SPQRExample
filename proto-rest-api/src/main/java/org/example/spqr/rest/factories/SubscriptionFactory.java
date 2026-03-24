package org.example.spqr.rest.factories;

import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.SubscriberInfo;
import org.example.spqr.models.domain.Subscription;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Collections.emptyList;

public class SubscriptionFactory {

    public static List<Subscription> create(Entity entity, Function<String, Entity> preconditionsSource) {
        List<String> preconditions = entity.preconditions().entityIds();
        if (preconditions == null || preconditions.isEmpty()) {
            return emptyList();
        }

        List<Subscription> subscriptions = new ArrayList<>(4);
        for (String preconditionId : preconditions) {
            Entity preconditionEntity = preconditionsSource.apply(preconditionId);
            if (preconditionEntity == null) {
                continue;
            }
            Subscription subscription = buildSubscription(
                    entity.entityId(), entity.status().createdAt(),
                    preconditionEntity.entityId(), preconditionEntity.status().createdAt()
            );
            subscriptions.add(subscription);
        }
        return subscriptions;
    }

    private static Subscription buildSubscription(String entityId,
                                                  ZonedDateTime createdAt,
                                                  String preconditionEntityId,
                                                  ZonedDateTime preconditionEntityCreatedAt) {
        SubscriberInfo subscriberInfo = SubscriberInfo.builder()
                .entityId(entityId)
                .entityCreatedAt(createdAt)
                .build();
        return Subscription.builder()
                .subscriptionId(UUID.randomUUID().toString())
                .entityId(preconditionEntityId)
                .entityCreatedAt(preconditionEntityCreatedAt)
                .subscriberInfo(subscriberInfo)
                .createdAt(ZonedDateTime.now())
                .build();
    }
}
