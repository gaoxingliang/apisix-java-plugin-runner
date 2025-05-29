/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.apisix.plugin.runner.filter.simple;

import org.apache.apisix.plugin.runner.*;
import org.apache.apisix.plugin.runner.db.model.*;
import org.apache.apisix.plugin.runner.filter.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;

import static org.apache.apisix.plugin.runner.filter.Constants.*;

/**
 * stage: ext-plugin-post-req
 * conf:
 * {
 *     "source": "SOURCE_XXX",
 *     "userId" : "17"
 * }
 */
@Component
public class RecordRequestFilter implements PluginFilter {
    private final Logger logger = LoggerFactory.getLogger(RecordRequestFilter.class);

    @Autowired
    LogService logService;

    @Override
    public String name() {
        return "RecordRequestFilter";
    }

    @Override
    public void filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
        logger.info("input apisix request id {} headers:{}, url:{}, raw input:{}, ", request.getRequestId(),
                request.getHeaders(), request.getPath(), StringUtils.abbreviate(request.getBody(), 32)
        );
        Map<String, String> configMap = RequestUtils.getConfigMap(request.getConfig(this));
        int userid = RequestUtils.getUserIdFromConfigMap(request, configMap);
        String source = RequestUtils.getSourceFromConfigMap(request, configMap);
        // set the internal request id
        String requestId = request.getHeader(HEADER_REQUEST_ID);
        if (StringUtils.isBlank(requestId)) {
            requestId = RequestUtils.newRequestId();
        }
        request.setHeader(HEADER_INTERNAL_REQUEST_ID, requestId);
        ApiLog log = new ApiLog();
        log.setUserid(userid);
        log.setMethod(request.getMethod().toString());
        log.setPath(request.getPath());
        log.setIp(request.getSourceIP());
        log.setRequestId(requestId);
        log.setSource(source);
        StringBuilder requestArgs = new StringBuilder(256);
        request.getArgs().forEach((k, v) -> requestArgs.append(k).append('=').append(v).append('&'));
        log.setRequestParameters(requestArgs.toString());
        log.setRequestBody(request.getBody());
        logService.logRequestStart(log);
        chain.filter(request, response);
    }


    /**
     * If you need to fetch request body in the current plugin, you will need to return true in this function.
     */
    @Override
    public Boolean requiredBody() {
        return true;
    }
}
