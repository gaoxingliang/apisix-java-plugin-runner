package org.apache.apisix.plugin.runner.kafka;

import lombok.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

import java.util.*;

@Getter
@ToString
@Configuration
public class KafkaConfiguration {
    @Value("${kafka.bootstrap}")
    String bootstrap;
    String keySerializer = StringSerializer.class.getCanonicalName();
    String valueSerializer = StringSerializer.class.getCanonicalName();
    String compressType = "zstd";
    @Value("${kafka.batchSize}")
    int batchSize;
    @Value("${kafka.lingerMs}")
    int lingerMs;
    @Value("${kafka.maxRequestSize}")
    int maxRequestSize;
    @Value("${kafka.maxBlockMs:30000}")
    int maxBlockMs;
    @Value("${kafka.sendTimeoutMs:10000}")
    int sendTimeoutMs;

    @Value("${kafka.apiLogTopic:apilog}")
    String apiLogTopic;

    @Bean
    public KafkaProducer<String, String> kafkaProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressType);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, maxBlockMs);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "apilog");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, maxRequestSize);

        return new KafkaProducer<>(props);
    }
}
