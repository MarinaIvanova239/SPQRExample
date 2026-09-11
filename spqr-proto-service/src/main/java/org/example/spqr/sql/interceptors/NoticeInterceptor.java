package org.example.spqr.sql.interceptors;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;

import java.sql.SQLWarning;
import java.sql.Statement;

import static org.slf4j.LoggerFactory.getLogger;

@Intercepts({
        // сейчас работает только для select запросов
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class})
})
public class NoticeInterceptor implements Interceptor {
    private static final Logger LOG = getLogger(NoticeInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } finally {
            Object[] args = invocation.getArgs();
            if (args != null && args.length > 0 && args[0] instanceof Statement statement) {
                StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
                String sql = statementHandler.getBoundSql().getSql().replaceAll("\\s+", " ").trim();
                SQLWarning warning = statement.getWarnings();
                while (warning != null) {
                    LOG.info("Notice/warning message '{}' received during execution of sql query '{}'", warning.getMessage(), sql);
                    warning = warning.getNextWarning();
                }
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
}
