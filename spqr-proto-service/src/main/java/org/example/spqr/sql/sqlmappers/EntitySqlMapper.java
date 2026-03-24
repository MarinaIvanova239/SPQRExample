package org.example.spqr.sql.sqlmappers;

import org.apache.ibatis.annotations.Param;
import org.example.spqr.models.dm.EntityChangeSetDm;
import org.example.spqr.models.dm.EntityDm;
import org.example.spqr.models.dm.EntitySearchSetDm;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("entitySqlMapper")
public interface EntitySqlMapper extends FlushableSqlMapper {

    void insert(EntityDm entity);

    List<EntityDm> selectByIds(@Param("searchSet") EntitySearchSetDm searchSet);

    boolean isEntityExists(@Param("searchSet") EntitySearchSetDm searchSet);

    void update(
            @Param("searchSet") EntitySearchSetDm searchSet,
            @Param("changeset") EntityChangeSetDm changeset
    );

    List<EntityDm> selectLastUpdated(@Param("bounds") MapperBounds mapperBounds, @Param("searchSet") EntitySearchSetDm searchSet);
}
