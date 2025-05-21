package org.apache.apisix.plugin.runner.filter;

import com.google.common.cache.*;
import org.apache.apisix.plugin.runner.db.model.*;
import org.slf4j.*;
import org.springframework.stereotype.*;

import java.time.*;

@Service
public class LogService {
    private Cache<String, ApiLog> requestIdCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .initialCapacity(1024).build();

    private final Logger logger = LoggerFactory.getLogger(LogService.class);

    public void logRequestStart(ApiLog log) {
        requestIdCache.put(log.getRequestId(), log);
    }

    public void logRequestEnd(String requestId, String status, int httpcode, String responsebody) {
        ApiLog apiLog = requestIdCache.getIfPresent(requestId);
        if (apiLog == null) {
            logger.warn("not found the request related request:{}", requestId);
        } else {
            apiLog.setStatus(status);
            apiLog.setCode(httpcode);
            apiLog.setResponse(responsebody);
            apiLog.setElapse(System.currentTimeMillis() -  apiLog.getTimestamp());
            logger.info("Finish request : {}", apiLog);
        }
    }
}
