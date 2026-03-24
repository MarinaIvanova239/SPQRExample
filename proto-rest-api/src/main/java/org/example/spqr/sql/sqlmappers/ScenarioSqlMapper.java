package org.example.spqr.sql.sqlmappers;

import org.apache.ibatis.annotations.Param;
import org.example.spqr.models.dm.ScenarioDm;
import org.springframework.stereotype.Component;

@Component("scenarioSqlMapper")
public interface ScenarioSqlMapper extends FlushableSqlMapper {

    void insert(@Param("scenario") ScenarioDm scenario);

    ScenarioDm selectByScenarioId(@Param("scenarioId") String scenarioId);
}