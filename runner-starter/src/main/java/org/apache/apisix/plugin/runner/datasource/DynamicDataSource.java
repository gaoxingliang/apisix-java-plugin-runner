package org.apache.apisix.plugin.runner.datasource;

import org.springframework.jdbc.datasource.lookup.*;

public class DynamicDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getDataSourceType();
    }
} 