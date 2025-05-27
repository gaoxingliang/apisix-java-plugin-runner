package org.apache.apisix.plugin.runner.datasource;

import com.zaxxer.hikari.*;
import org.springframework.boot.context.properties.*;
import org.springframework.boot.jdbc.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.*;

import javax.sql.*;
import java.util.*;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.logging")
    public DataSource loggingDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.apigateway")
    public DataSource apigatewayDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
    public DynamicDataSource dataSource(DataSource loggingDataSource, DataSource apigatewayDataSource) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceEnum.LOGGING.getValue(), loggingDataSource);
        targetDataSources.put(DataSourceEnum.APIGATEWAY.getValue(), apigatewayDataSource);
        DynamicDataSource ds = new DynamicDataSource();
        ds.setTargetDataSources(targetDataSources);

        return ds;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DynamicDataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
} 