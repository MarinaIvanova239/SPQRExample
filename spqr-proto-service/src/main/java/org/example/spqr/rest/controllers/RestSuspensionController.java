package org.example.spqr.rest.controllers;

import lombok.NoArgsConstructor;
import org.example.spqr.rest.service.EntityService;
import org.example.spqr.models.rq.EntityIdsRq;
import org.example.spqr.models.rs.EntityRs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@NoArgsConstructor
@RestController
@RequestMapping(path = "/suspensions")
public class RestSuspensionController {
    public static final String APPLICATION_JSON = "application/json;charset=utf-8";

    @Autowired
    private EntityService entityService;

    @PutMapping(consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<List<EntityRs>> suspendEntities(@RequestBody EntityIdsRq requests) {
        List<EntityRs> entityRms = entityService.suspendEntities(requests.getEntityIds());
        return ResponseEntity.ok()
                .body(entityRms);
    }
}
