package org.example.spqr.rest.controllers;

import lombok.NoArgsConstructor;
import org.example.spqr.rest.service.EventService;
import org.example.spqr.rest.service.InteractionService;
import org.example.spqr.models.rq.EntityIdsRq;
import org.example.spqr.models.rq.EventRq;
import org.example.spqr.models.rq.ResponseRq;
import org.example.spqr.models.rs.EventRs;
import org.example.spqr.models.rs.ResponseRs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@NoArgsConstructor
@RestController
@RequestMapping(path = "/interactions")
public class RestInteractionsController {
    public static final String APPLICATION_JSON = "application/json;charset=utf-8";

    @Autowired
    private InteractionService interactionService;
    @Autowired
    private EventService eventService;


    @GetMapping(path = "/events", produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
    public ResponseEntity<Map<String, EventRs>> getLatestEvents(@RequestBody EntityIdsRq request,
                                                                @RequestParam(value = "shardName", required = false) String shardName) {
        Map<String, EventRs> entityId2Events = eventService.getLatestByEntities(request.getEntityIds(), shardName);
        return ResponseEntity.ok()
                .body(entityId2Events);
    }

    @PostMapping(path = "/events/bulk", produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
    public ResponseEntity<Map<String, EventRs>> addEvents(@RequestBody Map<String, EventRq> requests) {
        Map<String, EventRs> entityId2Events = eventService.addEvents(requests);
        return ResponseEntity.ok()
                .body(entityId2Events);
    }

    @PostMapping(path = "/responses/bulk", produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
    public ResponseEntity<List<ResponseRs>> addCallbacks(@RequestBody List<ResponseRq> responses) {
        List<ResponseRs> addedResponses = interactionService.addResponses(responses);
        return ResponseEntity.ok()
                .body(addedResponses);
    }

    @PostMapping(path = "/responses", produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
    public ResponseEntity<ResponseRs> addCallback(@RequestBody ResponseRq response) {
        ResponseRs addedResponse = interactionService.addResponse(response);
        return ResponseEntity.ok()
                .body(addedResponse);
    }

    @GetMapping(path = "/responses/{entityId}", produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
    public ResponseEntity<List<ResponseRs>> getResponses(@PathVariable(name = "entityId") String entityId) {
        List<ResponseRs> responses = interactionService.getResponses(entityId);
        return ResponseEntity.ok()
                .body(responses);
    }
}
