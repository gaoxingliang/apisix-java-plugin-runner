package org.apache.apisix.plugin.runner.datasource;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynDataSource {
    DataSourceEnum value() default DataSourceEnum.LOGGING;
} 