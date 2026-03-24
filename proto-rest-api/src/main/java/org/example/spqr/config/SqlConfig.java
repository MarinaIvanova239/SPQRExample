package org.example.spqr.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.type.JdbcType;
import org.example.spqr.sql.transaction.SqlTransactionManager;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

import static org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRED;

@EnableTransactionManagement
@Configuration
@PropertySource("classpath:application.properties")
@MapperScan("org.example.spqr.sql.sqlmappers")
@ComponentScan("org.example.spqr.sql")
public class SqlConfig {
    @Value("${database.schema:crab}")
    String schema;
    @Value("${database.maxTotal:8}")
    Integer maxTotal;
    @Value("${database.maxLifetime:1800}")
    Integer maxLifetime;
    @Value("${database.minIdle:4}")
    Integer minIdle;
    @Value("${database.idleTimeout:600}")
    Integer idleTimeout;
    @Value("${database.lockTimeout:1000}")
    Integer lockTimeout;
    @Value("${database.statementTimeout:30}")
    Integer statementTimeout;
    @Value("${database.transactionTimeout:60}")
    Integer transactionTimeout;

    @Value("${database.jdbcUrl}")
    private String jdbcUrl;
    @Value("${database.username}")
    private String username;
    @Value("${database.password}")
    private String password;

    @Bean
    SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource,
                                            VendorDatabaseIdProvider databaseIdProvider,
                                            Properties sqlDbConfigurationProperties) throws IOException {
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        configuration.setDefaultStatementTimeout(statementTimeout);
        configuration.setDefaultExecutorType(ExecutorType.BATCH);

        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setConfiguration(configuration);
        sqlSessionFactoryBean.setTypeAliasesPackage("org.example.spqr.models.dm");
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/*-mapper.xml"));
        sqlSessionFactoryBean.setConfigurationProperties(sqlDbConfigurationProperties);
        sqlSessionFactoryBean.setDatabaseIdProvider(databaseIdProvider);
        return sqlSessionFactoryBean;
    }

    @Bean
    VendorDatabaseIdProvider databaseIdProvider() {
        Properties providerProperties = new Properties();
        providerProperties.setProperty("PostgreSQL", "postgres");
        VendorDatabaseIdProvider provider = new VendorDatabaseIdProvider();
        provider.setProperties(providerProperties);
        return provider;
    }

    @Bean
    SqlTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        dataSourceTransactionManager.setDefaultTimeout(transactionTimeout);
        return new SqlTransactionManager(dataSourceTransactionManager);
    }

    @Bean
    TransactionTemplate transactionOperations(SqlTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(transactionManager);
        transactionTemplate.setPropagationBehavior(PROPAGATION_REQUIRED);
        transactionTemplate.setIsolationLevel(ISOLATION_READ_COMMITTED);
        return transactionTemplate;
    }

    @Bean
    ThreadPoolTaskExecutor threadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setQueueCapacity(maxTotal);
        executor.setCorePoolSize(maxTotal);
        return executor;
    }

    @Bean
    ThreadPoolExecutor executorService(ThreadPoolTaskExecutor threadPoolExecutor) {
        return threadPoolExecutor.getThreadPoolExecutor();
    }

    @Bean
    HikariDataSource dataSource(HikariConfig hikariConfig) {
        return new HikariDataSource(hikariConfig);
    }

    @Bean
    HikariConfig hikariConfig() {
        Properties dataSourceProperties = new Properties();
        dataSourceProperties.setProperty("currentSchema", schema);
        dataSourceProperties.setProperty("logServerErrorDetail", "false");
        dataSourceProperties.setProperty("options", "-c lock_timeout=" + lockTimeout);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setPoolName("pool");
        hikariConfig.setAutoCommit(false);
        hikariConfig.setIdleTimeout(idleTimeout * 1000L);
        hikariConfig.setKeepaliveTime(60_000);
        hikariConfig.setMaxLifetime(maxLifetime * 1000L);
        hikariConfig.setMinimumIdle(minIdle);
        hikariConfig.setMaximumPoolSize(maxTotal);
        hikariConfig.setConnectionInitSql("");
        hikariConfig.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        hikariConfig.setLeakDetectionThreshold(2 * transactionTimeout * 1000L);
        hikariConfig.setRegisterMbeans(true);
        hikariConfig.setDataSourceProperties(dataSourceProperties);
        return hikariConfig;
    }
}