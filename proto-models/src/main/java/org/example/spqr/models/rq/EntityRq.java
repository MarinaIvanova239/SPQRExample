package org.example.spqr.models.rq;

import lombok.Data;

import java.util.List;

@Data
public class EntityRq {
    String entityId;
    String scenarioName;
    List<String> preconditions;
}