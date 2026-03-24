## Ошибки и примеры запросов

1. Мультишардовые select'ы (например GET /entities?limit=2&offset=0)

Пример sql запроса 
```
    SELECT *
    FROM ENTITIES ENT
    WHERE ENT.CREATED_AT >= date '2026-03-22' AND ENT.CREATED_AT < date '2026-03-25'
    ORDER BY UPDATED_AT DESC
    LIMIT 2 OFFSET 0;
```

Результат
```
    org.postgresql.util.PSQLException: ERROR: multishard state is out of sync
    at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2875) ~[postgresql-42.7.10.jar:42.7.10]
```

Иногда возникает такая ошибка

```
2026-03-23T16:12:15.449+03:00  WARN 236489 --- [nio-8085-exec-8] com.zaxxer.hikari.pool.ProxyConnection   : pool - Connection org.postgresql.jdbc.PgConnection@3d5c9c24 marked as broken because of SQLSTATE(08006), ErrorCode(0)  
 
org.postgresql.util.PSQLException: An I/O error occurred while sending to the backend.
    at org.postgresql.core.v3.QueryExecutorImpl.execute(QueryExecutorImpl.java:456) ~[postgresql-42.7.10.jar:42.7.10]
    at org.postgresql.jdbc.PgStatement.executeInternal(PgStatement.java:526) ~[postgresql-42.7.10.jar:42.7.10]
    at org.postgresql.jdbc.PgStatement.execute(PgStatement.java:436) ~[postgresql-42.7.10.jar:42.7.10]
    at org.postgresql.jdbc.PgPreparedStatement.executeWithFlags(PgPreparedStatement.java:196) ~[postgresql-42.7.10.jar:42.7.10]
    at org.postgresql.jdbc.PgPreparedStatement.execute(PgPreparedStatement.java:182) ~[postgresql-42.7.10.jar:42.7.10]
    at com.zaxxer.hikari.pool.ProxyPreparedStatement.execute(ProxyPreparedStatement.java:44) ~[HikariCP-4.0.3.jar:na]
    at com.zaxxer.hikari.pool.HikariProxyPreparedStatement.execute(HikariProxyPreparedStatement.java) ~[HikariCP-4.0.3.jar:na]
    at org.apache.ibatis.executor.statement.PreparedStatementHandler.query(PreparedStatementHandler.java:65) ~[mybatis-3.5.19.jar:3.5.19]
 
Caused by: java.net.SocketException: Broken pipe
    at java.base/sun.nio.ch.SocketDispatcher.write0(Native Method) ~[na:na]
    at java.base/sun.nio.ch.SocketDispatcher.write(SocketDispatcher.java:62) ~[na:na]
    at java.base/sun.nio.ch.NioSocketImpl.tryWrite(NioSocketImpl.java:394) ~[na:na]
    at java.base/sun.nio.ch.NioSocketImpl.implWrite(NioSocketImpl.java:410) ~[na:na]
    at java.base/sun.nio.ch.NioSocketImpl.write(NioSocketImpl.java:440) ~[na:na]
    at java.base/sun.nio.ch.NioSocketImpl$2.write(NioSocketImpl.java:819) ~[na:na]
    at java.base/java.net.Socket$SocketOutputStream.write(Socket.java:1195) ~[na:na]
    at org.postgresql.util.internal.PgBufferedOutputStream.flushBuffer(PgBufferedOutputStream.java:41) ~[postgresql-42.7.10.jar:42.7.10]
```

2. Мультишардовые операции в одной транзакции (select, insert в разные шарды + select из референсной таблицы)


```
    SELECT ENT.ENTITY_ID, ENT.STATE_ID
    FROM ENTITIES ENT
    LEFT OUTER JOIN SCENARIOS SCNR ON SCNR.SCENARIO_ID = ENT.SCENARIO_ID
    WHERE ENT.ENTITY_ID IN ('e1', 'e2')
        AND ENT.CREATED_AT >= date '2026-03-22'
        AND ENT.CREATED_AT < date '2026-03-25';
        
    SELECT SCENARIO_ID, CREATED_AT, NAME
    FROM ENTITY_SCENARIOS
    WHERE SCENARIO_ID = 'sc-id';
    
    INSERT INTO ENTITIES(
        ENTITY_ID,
        CREATED_AT,
        STATE_ID,
        UPDATED_AT,
        SCENARIO_ID
    ) VALUES (
        'entity-1',
        '2026-03-24 14:30:00',
        1,
        '2026-03-24 14:30:00',
        'scenario-1'
    );
```

```
### SQL: SELECT                   SCENARIO_ID,         CREATED_AT,         NAME     FROM ENTITY_SCENARIOS         WHERE SCENARIO_ID = ?
### Cause: org.postgresql.util.PSQLException: ERROR: multishard state is out of sync
; uncategorized SQLException; SQL state [SPQRU]; error code [0]; ERROR: multishard state is out of sync] with root cause
 
org.postgresql.util.PSQLException: ERROR: multishard state is out of sync

```

3. Несколько insert'ов в одной транзакции (даже в один шард)

```
INSERT INTO DELIVERIES(SUBSCRIPTION_ID, ENTITY_ID, ENTITY_CREATED_AT, DELIVERED_AT)
VALUES('subs-1', 'entity-1', '2026-03-24 12:46:00', '2026-03-24 14:30:00');
INSERT INTO DELIVERIES(SUBSCRIPTION_ID, ENTITY_ID, ENTITY_CREATED_AT, DELIVERED_AT)
VALUES('subs-2', 'entity-2', '2026-03-24 12:46:00', '2026-03-24 14:30:00');

```

```
Caused by: org.springframework.jdbc.UncategorizedSQLException: org.example.spqr.sql.mappers.DeliverySqlMapper.insert (batch index #1) failed. Cause: java.sql.BatchUpdateException: Batch entry 1 <unknown> was aborted: ERROR: extended xproto state out of sync  Call getNextException to see other errors in the batch.
; uncategorized SQLException; SQL state [SPQRU]; error code [0]; Batch entry 1 <unknown> was aborted: ERROR: extended xproto state out of sync  Call getNextException to see other errors in the batch.
    at org.mybatis.spring.MyBatisExceptionTranslator.translateExceptionIfPossible(MyBatisExceptionTranslator.java:96) ~[mybatis-spring-3.0.5.jar:3.0.5]
```

4. Select в один шард при недоступности другого шарда

```
org.postgresql.util.PSQLException: ERROR: shard sh2: failed to find primary within
    at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2875) ~[postgresql-42.7.10.jar:42.7.10]
```