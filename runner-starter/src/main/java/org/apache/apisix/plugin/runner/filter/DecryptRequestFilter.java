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

package org.apache.apisix.plugin.runner.filter;

import org.apache.apisix.plugin.runner.*;
import org.apache.apisix.plugin.runner.db.model.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;

import static org.apache.apisix.plugin.runner.filter.Constants.*;

/**
 * stage: ext-plugin-post-req
 */
@Component
public class DecryptRequestFilter implements PluginFilter {
    private final Logger logger = LoggerFactory.getLogger(DecryptRequestFilter.class);

    @Autowired
    UserService userService;

    @Autowired
    LogService logService;

    @Override
    public String name() {
        /* It is recommended to keep the name of the filter the same as the class name.
         Configure the filter to be executed on apisix's routes in the following format

        {
            "uri": "/hello",
            "plugins": {
                "ext-plugin-post-req": {
                    "conf": [{
                        "name": "RewriteRequestDemoFilter",
                        "value": "bar"
                    }]
                }
            },
            "upstream": {
                "nodes": {
                    "127.0.0.1:1980": 1
                },
                "type": "roundrobin"
            }
        }

        The value of name in the configuration corresponds to the value of return here.
         */

        return "DecryptRequestFilter";
    }

    @Override
    public void filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
        logger.info("input headers:{}, url:{}, raw input:{}, ", request.getHeaders(), request.getPath(), StringUtils.abbreviate(request.getBody(), 32));
        User user = userService.tryFindUser(request.getHeader(Constants.HEADER_USER_ID), User.PROVIDER_US);
        if (user == null) {
            response.setStatusCode(403);
            response.setBody(ERROR_NOT_FOUND);
            logger.warn("未找到用户：{}", request.getHeaders());
        } else {
            try {
                // set the internal request id
                request.setHeader(HEADER_SOURCE, HEADER_SOURCE_VALUE_SOURCE_DATA);
                String requestId = request.getHeader(HEADER_REQUEST_ID);
                if (StringUtils.isBlank(requestId)) {
                    requestId = UUID.randomUUID().toString().toLowerCase();
                }
                request.setHeader(HEADER_INTERNAL_REQUEST_ID, requestId);
                ApiLog log = new ApiLog();
                log.setUserid(user.getUserid());
                log.setMethod(request.getMethod().toString());
                log.setPath(request.getPath());
                log.setIp(request.getSourceIP());
                log.setRequestId(requestId);
                StringBuilder requestArgs = new StringBuilder(256);
                request.getArgs().forEach((k,v) -> requestArgs.append(k).append('=').append(v).append('&'));
                log.setRequestParameters(requestArgs.toString());

                String contentType = request.getHeader(HEADER_CONTENT_TYPE);
                if (contentType != null &&
                        (contentType.startsWith(Constants.HEADER_TYPE_MULTIPART_FORM) // if form with file
                                || contentType.startsWith(HEADER_TYPE_FORM_URLENCODED)) // if form without file
                ) {
                    // 如果是form类 暂时不加密
                    String encryptedFields = request.getHeader(HEADER_FORM_ENCRYPTED_FIELDS);
                    if ("none".equalsIgnoreCase(encryptedFields)) {
                        // 什么都不加解密
                        logger.info("DecryptRequestFilter：request:{}, user：{}，do nothing for form fields", request.getRequestId(), user.getUserid());
                        // form可能传文件。只记录更少的参数
                        log.setRequestBody(request.getBody());
                    } else {
                        throw new IllegalArgumentException("暂不支持指定对字段加密" + encryptedFields);
                    }
                } else {
                    String decryptedBody = userService.decryptBody(request.getBody(), user);
                    request.changeBody(decryptedBody);
                    log.setRequestBody(decryptedBody);
                    request.setHeader(HEADER_REQUESTBODY_ENCRYPTED_FLAG, "true");
                    logger.info("DecryptRequestFilter：request:{}, user：{}，{}", request.getRequestId(), user.getUserid(), StringUtils.abbreviate(decryptedBody, 128));
                }
                logService.logRequestStart(log);
            } catch (Exception e) {
                logger.error("decrypt request failure", e);
                response.setStatusCode(400);
                response.setBody(ERROR_DECRYPT_REQUEST_FAILURE);
            }
        }
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
