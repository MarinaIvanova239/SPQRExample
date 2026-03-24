package org.example.spqr.rest.mappers;

import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.Event;
import org.example.spqr.models.domain.Page;
import org.example.spqr.models.domain.Response;
import org.example.spqr.models.rs.ResponseRs;
import org.example.spqr.models.rs.EventRs;
import org.example.spqr.models.rs.ListInfoRs;
import org.example.spqr.models.rs.EntityRs;
import org.example.spqr.models.rs.PageRs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


public class Domain2ResponseMapper {

    public EntityRs from(Entity entity) {
        if (entity == null) {
            return null;
        }

        EntityRs entityRm = new EntityRs();
        entityRm.setEntityId(entity.entityId());
        entityRm.setStateId(entity.status().state().value());
        entityRm.setScenarioName(entity.scenarioSpec().name());
        return entityRm;
    }

    private ListInfoRs from(int limit, int offset) {
        ListInfoRs result = new ListInfoRs();
        result.setLimit(limit);
        result.setOffset(offset);
        return result;
    }

    public <D, RM> PageRs<RM> from(Page<D> page, Function<D, RM> mapper) {
        PageRs<RM> result = new PageRs<>();

        int size = 0, limit = 0, offset = 0;
        if (page != null) {
            size = page.getItems().size();
            limit = page.getLimit();
            offset = page.getOffset();
        }

        List<RM> items = new ArrayList<>(size);
        if (size > 0) {
            page.getItems().forEach(preview -> items.add(mapper.apply(preview)));
        }
        result.setItems(items);
        result.setListInfo(from(limit, offset));

        return result;
    }

    public PageRs<EntityRs> fromEntities(Page<Entity> entities) {
        return from(entities, this::from);
    }

    public EventRs from(Event event) {
        if (event == null) {
            return null;
        }

        EventRs eventRs = new EventRs();
        eventRs.setEventName(event.eventName());
        return eventRs;
    }

    public ResponseRs from(Response response) {
        if (response == null) {
            return null;
        }

        ResponseRs responseRs = new ResponseRs();
        responseRs.setRequestId(response.requestId());
        responseRs.setBody(response.body());
        return responseRs;
    }

    public List<ResponseRs> from(List<Response> responses) {
        return responses.stream()
                .map(this::from)
                .collect(toList());
    }

    public Map<String, EventRs> from(Map<String, Event> entity2EventMap) {
        return entity2EventMap.entrySet().stream()
                .filter(entry -> from(entry.getValue()) != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> from(entry.getValue())
                ));
    }

}
