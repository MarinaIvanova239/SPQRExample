package org.example.spqr.rest.controllers;

import lombok.NoArgsConstructor;
import org.example.spqr.rest.service.EntityService;
import org.example.spqr.models.rs.EntityRs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@NoArgsConstructor
@RestController
@RequestMapping(path = "/activations")
public class RestActivationController {
    public static final String APPLICATION_JSON = "application/json;charset=utf-8";

    @Autowired
    private EntityService entityService;

    @PostMapping(path = "/conditional", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<List<EntityRs>> activateConditional(@RequestParam(value = "entityId") String entityId) {
        List<EntityRs> entityRms = entityService.activateConditional(entityId);
        return ResponseEntity.ok()
                .body(entityRms);
    }

    @PostMapping(path = "/suspended", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<List<EntityRs>> activateSuspended(@RequestParam(value = "maxCount") int maxCount) {
        List<EntityRs> entityRms = entityService.activateSuspended(maxCount);
        return ResponseEntity.ok()
                .body(entityRms);
    }
}
