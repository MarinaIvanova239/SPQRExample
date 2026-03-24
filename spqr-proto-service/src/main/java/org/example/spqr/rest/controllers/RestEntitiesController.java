package org.example.spqr.rest.controllers;

import lombok.NoArgsConstructor;
import org.example.spqr.rest.service.EntityService;
import org.example.spqr.models.rq.EntityRq;
import org.example.spqr.models.rs.EntityRs;
import org.example.spqr.models.rs.PageRs;
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

@NoArgsConstructor
@RestController
@RequestMapping(path = "/entities")
public class RestEntitiesController {
    public static final String APPLICATION_JSON = "application/json;charset=utf-8";

    @Autowired
    private EntityService entityService;

    @PostMapping(consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<EntityRs> addEntity(@RequestBody EntityRq request) {
        EntityRs entityRm = entityService.addEntity(request);
        return ResponseEntity.ok()
                .body(entityRm);
    }

    @PostMapping(path = "/bulk", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<List<EntityRs>> addEntities(@RequestBody List<EntityRq> requests) {
        List<EntityRs> entityRms = entityService.addEntities(requests);

        return ResponseEntity.ok()
                .body(entityRms);
    }

    @GetMapping(path = "/{entityId}", produces = APPLICATION_JSON)
    public ResponseEntity<EntityRs> getEntity(@PathVariable(name = "entityId") String entityId) {
        EntityRs entityRm = entityService.getEntity(entityId);
        return ResponseEntity.ok()
                .body(entityRm);
    }

    @GetMapping(produces = APPLICATION_JSON)
    public ResponseEntity<PageRs<EntityRs>> getEntities(
            @RequestParam(value = "limit", defaultValue = "20", required = false) int limit,
            @RequestParam(value = "offset", defaultValue = "0", required = false) int offset
    ) {
        PageRs<EntityRs> foundEntities = entityService.getEntities(limit, offset);
        return ResponseEntity.ok()
                .body(foundEntities);
    }
}
