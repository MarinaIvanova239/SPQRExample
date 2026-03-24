package org.example.spqr.sql.dao;

import org.example.spqr.models.domain.Scenario;

public interface ScenarioDao {
    void add(Scenario scenario);

    Scenario get(String scenarioId);
}
