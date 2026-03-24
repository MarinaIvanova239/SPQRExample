package org.example.spqr.rest.mappers;

import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.EntityState;
import org.example.spqr.models.domain.EntityStatus;
import org.example.spqr.models.domain.Event;
import org.example.spqr.models.domain.Preconditions;
import org.example.spqr.models.domain.Response;
import org.example.spqr.models.domain.ScenarioSpec;
import org.example.spqr.models.rq.ResponseRq;
import org.example.spqr.models.rq.EventRq;
import org.example.spqr.models.rq.EntityRq;

import java.time.ZonedDateTime;

public class Request2DomainMapper {

    public Entity from(EntityRq request) {
        ZonedDateTime now = ZonedDateTime.now();
        return Entity.builder()
                .entityId(request.getEntityId())
                .status(EntityStatus.builder()
                        .createdAt(now)
                        .updatedAt(now)
                        .state(EntityState.NEW)
                        .build())
                .scenarioSpec(ScenarioSpec.builder()
                        .name(request.getScenarioName())
                        .build())
                .preconditions(Preconditions.builder()
                        .entityIds(request.getPreconditions())
                        .build())
                .build();
    }

    public Event from(EventRq eventRq, Entity entity) {
        ZonedDateTime now = ZonedDateTime.now();
        return Event.builder()
                .eventName(eventRq.getEventName())
                .registeredAt(now)
                .entityId(entity.entityId())
                .entityCreatedAt(entity.status().createdAt())
                .build();
    }

    public Response from(ResponseRq responseRq) {
        ZonedDateTime now = ZonedDateTime.now();
        return Response.builder()
                .requestId(responseRq.getRequestId())
                .body(responseRq.getBody())
                .createdAt(now)
                .build();
    }
}
