package org.apache.apisix.plugin.runner.filter;

import com.alibaba.fastjson.*;
import com.google.common.cache.*;
import lombok.experimental.*;
import org.apache.apisix.plugin.runner.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.*;

import java.time.*;
import java.util.*;

@UtilityClass
public class RequestUtils {

    private Cache<Integer, Map<String, String>> configMapCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofHours(24))
            .initialCapacity(32).build();

    public String getFirstHeaderValue(Map<String, List<String>> headers, String header) {
        List<String> values = headers.get(header);

        return CollectionUtils.isEmpty(values) ? null : values.get(0);
    }

    public Map<String, String> getConfigMap(String configStr) {
        if (StringUtils.isEmpty(configStr)) {
            return new HashMap<>();
        } else {
            Map<String, String> configMap = configMapCache.getIfPresent(configStr.hashCode());
            if (configMap == null) {
                JSONObject conf = JSONObject.parseObject(configStr);
                Map<String, String> parsed = new HashMap<>();
                conf.entrySet().forEach(en -> {
                    parsed.put(en.getKey(), en.getValue().toString());
                });
                configMapCache.put(configStr.hashCode(), parsed);
            }

            return configMap;
        }
    }

    public String getSourceFromConfigMap(HttpRequest request,  Map<String, String> map) {
        String ret = map.get(Constants.RECORD_REQ_FILTER_ARG_SOURCE);
        if (ret == null) {
            throw new IllegalStateException("Cant get the request source from arg map : " + map);
        }

        return ret;
    }

    public int getUserIdFromConfigMap(HttpRequest request,  Map<String, String> map) {
        String ret = map.get(Constants.RECORD_REQ_FILTER_ARG_USERID);
        if (ret == null) {
            throw new IllegalStateException("Cant get the request userid from arg map : " + map);
        }

        return Integer.parseInt(ret);
    }

    public String getSourceFromConfigMap(PostRequest request,  Map<String, String> map) {
        String ret = map.get(Constants.RECORD_REQ_FILTER_ARG_SOURCE);
        if (ret == null) {
            throw new IllegalStateException("Cant get the request source from arg map : " + map);
        }

        return ret;
    }

    public int getUserIdFromConfigMap(PostRequest request,  Map<String, String> map) {
        String ret = map.get(Constants.RECORD_REQ_FILTER_ARG_USERID);
        if (ret == null) {
            throw new IllegalStateException("Cant get the request userid from arg map : " + map);
        }

        return Integer.parseInt(ret);
    }

    public String newRequestId() {
        return UUID.randomUUID().toString().toLowerCase();
    }
}
