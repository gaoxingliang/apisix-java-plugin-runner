package org.apache.apisix.plugin.runner.filter;

import com.alibaba.fastjson2.*;
import com.github.yitter.contract.*;
import com.github.yitter.idgen.*;
import com.google.common.cache.*;
import org.apache.apisix.plugin.runner.db.*;
import org.apache.apisix.plugin.runner.db.model.*;
import org.apache.apisix.plugin.runner.kafka.*;
import org.apache.kafka.clients.producer.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.annotation.*;
import java.time.*;
import java.util.concurrent.*;

@Service
public class LogService {
    private Cache<String, ApiLog> requestIdCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .initialCapacity(1024).build();

    @Value("${apilog.enable:true}")
    boolean apilogEnable;

    @Autowired
    KafkaProducer<String, String> kafkaProducer;
    @Autowired
    KafkaConfiguration kafkaConfiguration;
    @Autowired
    KafkaDelayAccessRecordDao kafkaDelayAccessRecordDao;

    @PostConstruct
    public void postConstruct() {
        IdGeneratorOptions options = new IdGeneratorOptions((short) 1);
        YitIdHelper.setIdGenerator(options);
    }

    private final Logger logger = LoggerFactory.getLogger(LogService.class);

    public void logRequestStart(ApiLog log) {
        if (apilogEnable) {
            requestIdCache.put(log.getRequestId(), log);
        }
    }

    public void logRequestEnd(String requestId, String status, int httpcode, String responsebody) {
        if (!apilogEnable) {
            return;
        }
        ApiLog apiLog = requestIdCache.getIfPresent(requestId);
        if (apiLog == null) {
            logger.warn("not found the request related request:{}", requestId);
        } else {
            apiLog.setStatus(status);
            apiLog.setCode(httpcode);
            apiLog.setResponse(responsebody);
            apiLog.setElapse(System.currentTimeMillis() -  apiLog.getTimestamp());
            apiLog.setRawkey(YitIdHelper.nextId() + "");
            String kafkaValue = JSONObject.toJSONString(apiLog);
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    kafkaConfiguration.getApiLogTopic(),
                    apiLog.getRawkey(),
                    kafkaValue
            );
            try {
                kafkaProducer.send(record).get(kafkaConfiguration.getSendTimeoutMs(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logger.error("Fail to send the record to kafka: {}", apiLog, e);
                KafkaDelayAccessRecord delayRecord = new KafkaDelayAccessRecord();
                delayRecord.setContent(kafkaValue);
                delayRecord.setTopic(kafkaConfiguration.getApiLogTopic());
                kafkaDelayAccessRecordDao.insert(delayRecord);
                logger.info("成功插入数据库：{},{}", delayRecord.getId());
            }
            logger.info("Finish request : {}", apiLog);
            requestIdCache.invalidate(requestId);
        }
    }
}
