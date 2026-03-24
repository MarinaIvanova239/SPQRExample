package org.example.spqr.sql.sqlmappers;

import org.apache.ibatis.annotations.Param;
import org.example.spqr.models.dm.EntityDm;
import org.example.spqr.models.dm.EntitySearchSetDm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component("activationSqlMapper")
public interface ActivationSqlMapper extends FlushableSqlMapper {

    List<EntityDm> selectEntitiesWithExpiredActivatedAt(
            @Param("stateId") int stateId,
            @Param("currentDate") Date currentDate,
            @Param("searchSet") EntitySearchSetDm searchSet,
            @Param("maxCount") int maxCount
    );

    void updateActivatedAt(
            @Param("entity") EntityDm entity,
            @Param("searchSet") EntitySearchSetDm searchSet);

    List<EntityDm> selectBySearchSetAndStateId(
            @Param("searchSet") EntitySearchSetDm searchSet,
            @Param("stateId") int stateId);

    List<EntityDm> selectBySearchSet(
            @Param("searchSet") EntitySearchSetDm searchSet);
}