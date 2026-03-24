package org.example.spqr.sql.dao.impl;

import jakarta.annotation.Resource;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.spqr.models.dm.ScenarioDm;
import org.example.spqr.models.domain.Scenario;
import org.example.spqr.sql.dao.ScenarioDao;
import org.example.spqr.sql.mappers.Data2DomainMapper;
import org.example.spqr.sql.mappers.Domain2DataMapper;
import org.example.spqr.sql.sqlmappers.ScenarioSqlMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;
import static org.slf4j.LoggerFactory.getLogger;

@RequiredArgsConstructor
@Component("scenarioDao")
@CacheConfig(cacheNames = {"scenarios"})
public class SqlScenarioDao implements ScenarioDao {

    private static final Logger LOG = getLogger(SqlScenarioDao.class);

    @Autowired
    private final Domain2DataMapper domain2DataMapper;
    @Autowired
    private final Data2DomainMapper data2DomainMapper;
    @Resource(name = "scenarioSqlMapper")
    private final ScenarioSqlMapper scenarioSqlMapper;

    @Override
    public void add(@NonNull Scenario scenario) {
        LOG.debug("Adding scenario with id={} into database...", scenario.id());
        ScenarioDm scenarioDm = domain2DataMapper.from(scenario);
        scenarioSqlMapper.insert(scenarioDm);
    }

    @Override
    @Cacheable(unless = "#result == null")
    public Scenario get(@NonNull String scenarioId) {
        LOG.debug("Reading scenario with id={} from database...", scenarioId);
        ScenarioDm scenarioDm = scenarioSqlMapper.selectByScenarioId(scenarioId);
        if (isNull(scenarioDm)) {
            return null;
        }
        return data2DomainMapper.from(scenarioDm);
    }
}
