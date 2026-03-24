package org.example.spqr.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.example.spqr.models.domain.Scenario;
import org.example.spqr.exceptions.SystemException;
import org.example.spqr.sql.dao.ScenarioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

import static java.util.Objects.nonNull;

@Component("scenarioService")
public class ScenarioService {

    private static final HashFunction HASH_FUNCTION = Hashing.murmur3_128();

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ScenarioDao scenarioDao;

    public Scenario register(String scenarioName) {
        String scenarioId = resolveObjectHash(scenarioName);
        Scenario lastScenario = scenarioDao.get(scenarioId);
        if (nonNull(lastScenario)) {
            return lastScenario;
        }
        Scenario newScenario = Scenario.builder()
                .id(scenarioId)
                .name(scenarioName)
                .createdAt(ZonedDateTime.now())
                .build();
        scenarioDao.add(newScenario);
        return newScenario;
    }

    protected String resolveObjectHash(Object object) {
        if (object == null) {
            return null;
        }
        try {
            String objectJson = objectMapper.writeValueAsString(object);
            HashCode hc = HASH_FUNCTION.hashString(objectJson, Charsets.UTF_8);
            return Long.toHexString(hc.asLong());
        } catch (JsonProcessingException e) {
            throw new SystemException(e);
        }
    }
}
