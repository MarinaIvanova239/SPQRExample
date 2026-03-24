package org.example.spqr.sql.sqlmappers;

import org.apache.ibatis.annotations.Flush;
import org.apache.ibatis.executor.BatchResult;

import java.util.List;

interface FlushableSqlMapper {

    @Flush
    List<BatchResult> flush();
}