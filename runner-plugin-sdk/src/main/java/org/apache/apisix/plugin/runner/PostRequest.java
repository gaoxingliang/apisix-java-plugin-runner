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

package org.apache.apisix.plugin.runner;

import io.github.api7.A6.HTTPRespCall.*;
import io.github.api7.A6.*;
import org.apache.apisix.plugin.runner.filter.*;
import org.springframework.util.*;

import java.nio.*;
import java.nio.charset.*;
import java.util.*;

public class PostRequest implements A6Request {
    private final Req req;

    private Long requestId;

    private Map<String, String> config;

    private Map<String, String> headers;

    private Integer status;

    private byte[] body;

    private Map<String, String> vars;

    public PostRequest(Req req) {
        this.req = req;
    }

    public static PostRequest from(ByteBuffer body) {
        Req req = Req.getRootAsReq(body);
        return new PostRequest(req);
    }

    @Override
    public byte getType() {
        return 4;
    }

    public long getConfToken() {
        return req.confToken();
    }

    public long getRequestId() {
        if (Objects.isNull(requestId)) {
            requestId = req.id();
        }
        return requestId;
    }

    public void initCtx(Map<String, String> config) {
        this.config = config;
    }

    public String getConfig(PluginFilter filter) {
        return config.getOrDefault(filter.name(), null);
    }

    public Map<String, String> getUpstreamHeaders() {
        if (Objects.isNull(headers)) {
            headers = new HashMap<>();
            for (int i = 0; i < req.headersLength(); i++) {
                TextEntry header = req.headers(i);
                headers.put(header.name(), header.value());
            }
        }
        return headers;
    }

    public Map<String, List<String>> getUpstreamHeadersMap() {
        Map<String, List<String>> multipleValueMap = new HashMap<>();
        for (int i = 0; i < req.headersLength(); i++) {
            TextEntry header = req.headers(i);
            multipleValueMap.computeIfAbsent(header.name(), k -> new ArrayList<>()).add( header.value());
        }
        return multipleValueMap;
    }


    public Integer getUpstreamStatusCode() {
        if (Objects.isNull(status)) {
            status = req.status();
        }
        return status;
    }

    public void setBody(String body) {
        this.body = body.getBytes();
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getBody() {
        return new String(body);
    }

    public String getBody(Charset charset) {
        return new String(body, charset);
    }

    public String getVars(String key) {
        if (CollectionUtils.isEmpty(vars)) {
            return null;
        }
        return vars.get(key);
    }

    public void setVars(Map<String, String> vars) {
        this.vars = vars;
    }
}
