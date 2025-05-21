package org.apache.apisix.plugin.runner.filter;

import lombok.experimental.*;
import org.springframework.util.*;

import java.util.*;

@UtilityClass
public class RequestUtils {

    public String getFirstHeaderValue(Map<String, List<String>> headers, String header) {
        List<String> values = headers.get(header);

        return CollectionUtils.isEmpty(values) ? null : values.get(0);
    }
}
