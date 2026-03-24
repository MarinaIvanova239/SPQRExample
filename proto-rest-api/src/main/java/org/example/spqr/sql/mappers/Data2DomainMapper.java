package org.example.spqr.sql.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.example.spqr.models.dm.PreconditionsDm;
import org.example.spqr.models.dm.ResponseDm;
import org.example.spqr.models.dm.DeliveryDm;
import org.example.spqr.models.dm.EventDm;
import org.example.spqr.models.dm.EntityDm;
import org.example.spqr.models.dm.RequestDm;
import org.example.spqr.models.dm.ScenarioDm;
import org.example.spqr.models.dm.SubscriberInfoDm;
import org.example.spqr.models.dm.SubscriptionDm;
import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.Preconditions;
import org.example.spqr.models.domain.Response;
import org.example.spqr.models.domain.Delivery;
import org.example.spqr.models.domain.Event;
import org.example.spqr.models.domain.EntityState;
import org.example.spqr.models.domain.EntityStatus;
import org.example.spqr.models.domain.Request;
import org.example.spqr.models.domain.Scenario;
import org.example.spqr.models.domain.ScenarioSpec;
import org.example.spqr.models.domain.SubscriberInfo;
import org.example.spqr.models.domain.Subscription;
import org.example.spqr.exceptions.SystemException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.example.spqr.utils.DateUtils.toZonedDateTime;

@Setter
@Component("data2DomainMapper")
public class Data2DomainMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final TypeReference<SubscriberInfoDm> subscriberInfoRef = new TypeReference<>() {};

    private final TypeReference<PreconditionsDm> preconditionsRef = new TypeReference<>() {};

    public Scenario from(ScenarioDm scenarioDm) {
        ZonedDateTime createdAt = toZonedDateTime(scenarioDm.getCreatedAt());
        return Scenario.builder()
                .id(scenarioDm.getId())
                .name(scenarioDm.getName())
                .createdAt(createdAt)
                .build();
    }

    public List<Entity> from(EntityDm... entityDms) {
        return Stream.of(entityDms).map(this::from).collect(toList());
    }

    public Entity from(EntityDm entityDm) {
        if (entityDm == null) {
            return null;
        }

        return Entity.builder()
                .entityId(entityDm.getEntityId())
                .status(buildStatus(entityDm))
                .activatedAt(toZonedDateTime(entityDm.getActivatedAt()))
                .scenarioSpec(buildScenario(entityDm))
                .preconditions(buildPreconditions(entityDm))
                .build();
    }

    private Preconditions buildPreconditions(EntityDm entityDm) {
        PreconditionsDm subscriberInfoDm = fromJson(entityDm.getPreconditions(), preconditionsRef);
        if (subscriberInfoDm == null) {
            return Preconditions.builder()
                    .entityIds(emptyList())
                    .subscriptionsCount(0)
                    .build();
        }
        return Preconditions.builder()
                .entityIds(subscriberInfoDm.getEntityIds())
                .subscriptionsCount(subscriberInfoDm.getSubscriptionsCount())
                .build();
    }

    private ScenarioSpec buildScenario(EntityDm entityDm) {
        return ScenarioSpec.builder()
                .id(entityDm.getScenarioId())
                .name(entityDm.getScenarioName())
                .build();
    }

    private EntityStatus buildStatus(EntityDm entityDm) {
        return EntityStatus.builder()
                .createdAt(toZonedDateTime(entityDm.getCreatedAt()))
                .updatedAt(toZonedDateTime(entityDm.getUpdatedAt()))
                .state(EntityState.toEntityState(entityDm.getStateId()))
                .build();
    }

    public Event from(EventDm eventDm) {
        if (eventDm == null) {
            return null;
        }

        return Event.builder()
                .eventName(eventDm.getEventName())
                .registeredAt(toZonedDateTime(eventDm.getRegisteredAt()))
                .entityId(eventDm.getEntityId())
                .entityCreatedAt(toZonedDateTime(eventDm.getEntityCreatedAt()))
                .build();
    }

    public List<Event> from(EventDm... eventDms) {
        if (isEmpty(eventDms)) {
            return emptyList();
        }
        return Stream.of(eventDms).map(this::from).collect(toList());
    }

    public Request from(RequestDm requestDm) {
        if (isNull(requestDm)) {
            return null;
        }

        return Request.builder()
                .requestId(requestDm.getRequestId())
                .createdAt(toZonedDateTime(requestDm.getCreatedAt()))
                .entityId(requestDm.getEntityId())
                .entityCreatedAt(toZonedDateTime(requestDm.getEntityCreatedAt()))
                .build();
    }

    public List<Request> from(RequestDm... requestDms) {
        return Stream.of(requestDms).map(this::from).collect(toList());
    }

    public Response from(ResponseDm callbackDm) {
        if (isNull(callbackDm)) {
            return null;
        }

        return Response.builder()
                .requestId(callbackDm.getRequestId())
                .createdAt(toZonedDateTime(callbackDm.getCreatedAt()))
                .entityId(callbackDm.getEntityId())
                .entityCreatedAt(toZonedDateTime(callbackDm.getEntityCreatedAt()))
                .build();
    }

    public List<Response> from(ResponseDm... callbackDmArr) {
        if (isEmpty(callbackDmArr)) {
            return emptyList();
        }
        return Stream.of(callbackDmArr).map(this::from).collect(toList());
    }

    public Subscription from(SubscriptionDm subscriptionDm) {
        if (isNull(subscriptionDm)) {
            return null;
        }

        SubscriberInfoDm subscriberInfoDm = fromJson(subscriptionDm.getSubscriberInfo(), subscriberInfoRef);

        return Subscription.builder()
                .subscriptionId(subscriptionDm.getSubscriptionId())
                .entityCreatedAt(toZonedDateTime(subscriptionDm.getEntityCreatedAt()))
                .createdAt(toZonedDateTime(subscriptionDm.getCreatedAt()))
                .entityId(subscriptionDm.getEntityId())
                .subscriberInfo(SubscriberInfo.builder()
                        .entityId(subscriberInfoDm.getEntityId())
                        .entityCreatedAt(toZonedDateTime(subscriberInfoDm.getEntityCreatedAt()))
                        .build())
                .build();
    }

    public List<Subscription> from(List<SubscriptionDm> subscriptionDms) {
        if (subscriptionDms.isEmpty()) {
            return emptyList();
        }
        return subscriptionDms.stream().map(this::from).collect(toList());
    }

    public Delivery from(DeliveryDm deliveryDm) {
        return Delivery.builder()
                .entityId(deliveryDm.getEntityId())
                .entityCreatedAt(toZonedDateTime(deliveryDm.getEntityCreatedAt()))
                .deliveredAt(toZonedDateTime(deliveryDm.getDeliveredAt()))
                .subscriptionId(deliveryDm.getSubscriptionId())
                .build();
    }

    <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (isNullOrEmpty(json)) {
            return null;
        }
        try {
            // Преобразование к массиву байт выполняется с целью обойти необоснованное
            // замечание SAST-анализатора 'XML External Entity'
            return objectMapper.readValue(json.getBytes(UTF_8), typeRef);
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }
}
