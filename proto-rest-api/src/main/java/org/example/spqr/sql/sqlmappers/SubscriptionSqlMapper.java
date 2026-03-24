package org.example.spqr.sql.sqlmappers;

import org.apache.ibatis.annotations.Param;
import org.example.spqr.models.dm.EntitySearchSetDm;
import org.example.spqr.models.dm.SubscriptionDm;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("subscriptionSqlMapper")
public interface SubscriptionSqlMapper extends FlushableSqlMapper {

    void insert(SubscriptionDm subscriptionDm);

    List<SubscriptionDm> selectBySearchSet(@Param("searchSet") EntitySearchSetDm searchSet);
}
