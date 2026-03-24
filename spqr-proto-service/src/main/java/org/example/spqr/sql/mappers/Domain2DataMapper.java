package org.example.spqr.sql.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.example.spqr.models.dm.ResponseDm;
import org.example.spqr.models.dm.DeliveryDm;
import org.example.spqr.models.dm.EventDm;
import org.example.spqr.models.dm.EntityChangeSetDm;
import org.example.spqr.models.dm.EntityDm;
import org.example.spqr.models.dm.RequestDm;
import org.example.spqr.models.dm.ScenarioDm;
import org.example.spqr.models.dm.SubscriberInfoDm;
import org.example.spqr.models.dm.SubscriptionDm;
import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.Response;
import org.example.spqr.models.domain.Delivery;
import org.example.spqr.models.domain.Event;
import org.example.spqr.models.domain.EntityChangeSet;
import org.example.spqr.models.domain.EntityStatus;
import org.example.spqr.models.domain.Request;
import org.example.spqr.models.domain.Scenario;
import org.example.spqr.models.domain.SubscriberInfo;
import org.example.spqr.models.domain.Subscription;
import org.example.spqr.exceptions.SystemException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.example.spqr.utils.DateUtils.toDateOrNull;

@Setter
@Component("domain2DataMapper")
public class Domain2DataMapper {

    public EntityChangeSetDm from(EntityChangeSet changeSet) {
        EntityChangeSetDm changeSetDm = new EntityChangeSetDm();

        if (changeSet.getStatus().isPresent()) {
            EntityStatus status = changeSet.getStatus().get();
            changeSetDm.setStatusUpdated(true);
            changeSetDm.setStateId(status.state().value());
            changeSetDm.setUpdatedAt(toDateOrNull(status.updatedAt()));
        }

        if (changeSet.getActivatedAt().isPresent()) {
            changeSetDm.setActivatedAtUpdated(true);
            changeSetDm.setActivatedAt(toDateOrNull(changeSet.getActivatedAt().get()));
        }
        return changeSetDm;
    }

    public ScenarioDm from(Scenario scenario) {
        ScenarioDm scenarioDm = new ScenarioDm();
        scenarioDm.setId(scenario.id());
        scenarioDm.setName(scenario.name());
        scenarioDm.setCreatedAt(Date.from(scenario.createdAt().toInstant()));
        return scenarioDm;
    }

    public EntityDm from(Entity entity) {
        EntityDm entityDm = new EntityDm();
        entityDm.setEntityId(entity.entityId());
        entityDm.setCreatedAt(toDateOrNull(entity.status().createdAt()));
        entityDm.setUpdatedAt(toDateOrNull(entity.status().updatedAt()));
        entityDm.setActivatedAt(toDateOrNull(entity.activatedAt()));
        entityDm.setStateId(entity.status().state().value());
        entityDm.setScenarioId(entity.scenarioSpec().id());
        return entityDm;
    }

    public List<EntityDm> from(Entity... entities) {
        if (isEmpty(entities)) {
            return emptyList();
        }
        return Arrays.stream(entities).map(this::from).toList();
    }

    public EventDm from(Event event, String entityId, Date entityCreatedAt) {
        if (event == null) {
            return null;
        }

        EventDm eventDm = new EventDm();
        eventDm.setEntityId(entityId);
        eventDm.setEntityCreatedAt(entityCreatedAt);
        eventDm.setEventName(event.eventName());
        eventDm.setRegisteredAt(toDateOrNull(event.registeredAt()));
        return eventDm;
    }

    public List<EventDm> from(Map<Entity, Event> eventMap) {
        if (isEmpty(eventMap)) {
            return emptyList();
        }

        return eventMap.entrySet().stream()
                .map(entry -> {
                    Entity entity = entry.getKey();
                    Event event = entry.getValue();
                    return from(event, entity.entityId(), toDateOrNull(entity.status().createdAt()));
                }).collect(toList());
    }

    public ResponseDm from(Response response, Request request) {
        if (response == null) {
            return null;
        }

        ResponseDm callbackDm = new ResponseDm();
        callbackDm.setEntityId(request.entityId());
        callbackDm.setEntityCreatedAt(toDateOrNull(request.entityCreatedAt()));
        callbackDm.setRequestId(response.requestId());
        callbackDm.setCreatedAt(toDateOrNull(response.createdAt()));
        callbackDm.setBody(response.body());
        return callbackDm;
    }

    public RequestDm from(Request request) {
        if (request == null) {
            return null;
        }

        RequestDm requestDm = new RequestDm();
        requestDm.setRequestId(request.requestId());
        requestDm.setCreatedAt(toDateOrNull(request.createdAt()));
        requestDm.setEntityCreatedAt(toDateOrNull(request.entityCreatedAt()));
        requestDm.setEntityId(request.entityId());
        return requestDm;
    }

    public SubscriptionDm from(Subscription subscription) {
        if (subscription == null) {
            return null;
        }

        SubscriptionDm subscriptionDm = new SubscriptionDm();
        subscriptionDm.setSubscriptionId(subscription.subscriptionId());
        subscriptionDm.setCreatedAt(toDateOrNull(subscription.createdAt()));
        subscriptionDm.setEntityId(subscription.entityId());
        subscriptionDm.setEntityCreatedAt(toDateOrNull(subscription.entityCreatedAt()));
        subscriptionDm.setSubscriberInfo(MappingUtils.toJsonOrNull(from(subscription.subscriberInfo())));
        return subscriptionDm;
    }

    public SubscriberInfoDm from(SubscriberInfo subscriberInfo) {
        if (subscriberInfo == null) {
            return null;
        }

        SubscriberInfoDm subscriberInfoDm = new SubscriberInfoDm();
        subscriberInfoDm.setEntityId(subscriberInfo.entityId());
        subscriberInfoDm.setEntityCreatedAt(toDateOrNull(subscriberInfo.entityCreatedAt()));
        return subscriberInfoDm;
    }

    public DeliveryDm from(Delivery delivery) {
        if (delivery == null) {
            return null;
        }

        DeliveryDm deliveryDm = new DeliveryDm();
        deliveryDm.setEntityCreatedAt(toDateOrNull(delivery.entityCreatedAt()));
        deliveryDm.setEntityId(delivery.entityId());
        deliveryDm.setDeliveredAt(toDateOrNull(delivery.deliveredAt()));
        deliveryDm.setSubscriptionId(delivery.subscriptionId());
        return deliveryDm;
    }

    public EventDm from(Event event) {
        if (event == null) {
            return null;
        }

        EventDm eventDm = new EventDm();
        eventDm.setEventName(event.eventName());
        eventDm.setRegisteredAt(toDateOrNull(event.registeredAt()));
        eventDm.setEntityId(event.entityId());
        eventDm.setEntityCreatedAt(toDateOrNull(event.entityCreatedAt()));
        return eventDm;
    }

    static class MappingUtils {
        private static final ObjectMapper objectMapper = new ObjectMapper();

        static String toJsonOrNull(Object value) {
            try {
                return nonNull(value) ? objectMapper.writeValueAsString(value) : null;
            } catch (IOException e) {
                throw new SystemException(e);
            }
        }
    }
}
