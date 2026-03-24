package org.example.spqr.sql.sqlmappers;

import org.apache.ibatis.annotations.Param;
import org.example.spqr.models.dm.EventDm;
import org.example.spqr.models.dm.EntitySearchSetDm;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("eventSqlMapper")
public interface EventSqlMapper extends FlushableSqlMapper {

    void insert(EventDm eventDm);

    List<EventDm> selectByEntitiesSearchSet(@Param("searchSet") EntitySearchSetDm searchSet);

    List<EventDm> selectLatestByEntities(@Param("searchSet") EntitySearchSetDm searchSet,
                                         @Param("shardHint") String shardHint);

}
