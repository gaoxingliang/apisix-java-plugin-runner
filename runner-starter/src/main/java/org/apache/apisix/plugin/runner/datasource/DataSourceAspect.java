package org.apache.apisix.plugin.runner.datasource;

import org.aspectj.lang.*;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.*;
import org.springframework.core.annotation.*;
import org.springframework.stereotype.*;

import java.lang.reflect.*;

@Aspect
@Order(1)
@Component
public class DataSourceAspect {

    @Pointcut("@annotation(org.apache.apisix.plugin.runner.datasource.DynDataSource)")
    public void dataSourcePointCut() {}

    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        DynDataSource ds = method.getAnnotation(DynDataSource.class);
        if (ds != null) {
            DynamicDataSourceContextHolder.setDataSourceType(ds.value().getValue());
        }

        try {
            return point.proceed();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceType();
        }
    }
} 