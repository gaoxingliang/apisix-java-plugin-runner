package org.apache.apisix.plugin.runner.datasource;

import org.apache.commons.lang3.*;

public class DynamicDataSourceContextHolder {
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void setDataSourceType(String dataSourceType) {
        CONTEXT_HOLDER.set(dataSourceType);
    }

    public static String getDataSourceType() {
        return ObjectUtils.firstNonNull(CONTEXT_HOLDER.get(), DataSourceEnum.LOGGING.getValue());
    }

    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
    }
} 