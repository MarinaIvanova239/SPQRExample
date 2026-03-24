package org.example.spqr.sql.sqlmappers;

import org.apache.ibatis.annotations.Param;
import org.example.spqr.models.dm.ResponseDm;
import org.example.spqr.models.dm.EntitySearchSetDm;
import org.example.spqr.models.dm.RequestDm;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("interactionSqlMapper")
public interface InteractionSqlMapper extends FlushableSqlMapper {

    void insertRequest(RequestDm request);

    void insertResponse(ResponseDm response);

    List<ResponseDm> selectResponsesBySearchSet(
            @Param("searchSet") EntitySearchSetDm searchSet);

    List<RequestDm> selectRequestsByIdAndSearchSet(
            @Param("requestId") String requestId,
            @Param("searchSet") EntitySearchSetDm searchSet);
}
