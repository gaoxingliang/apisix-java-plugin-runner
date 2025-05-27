package org.apache.apisix.plugin.runner.datasource;

public enum DataSourceEnum {
    LOGGING("logging"),
    APIGATEWAY("apigateway");

    private final String value;

    DataSourceEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
} 