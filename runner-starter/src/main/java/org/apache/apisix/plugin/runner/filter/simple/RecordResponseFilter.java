package org.apache.apisix.plugin.runner.filter.simple;

import cn.hutool.core.map.*;
import cn.hutool.core.util.*;
import org.apache.apisix.plugin.runner.*;
import org.apache.apisix.plugin.runner.filter.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.nio.charset.*;
import java.util.*;

/**
 * use this with {@link RecordRequestFilter}
 */
@Component
public class RecordResponseFilter implements PluginFilter {
    private final Logger logger = LoggerFactory.getLogger(RecordResponseFilter.class);

    @Autowired
    LogService logService;

    @Override
    public String name() {
        return "RecordResponseFilter";
    }

    @Override
    public void postFilter(PostRequest request, PostResponse response, PluginFilterChain chain) {
        Map<String, List<String>> headers = new CaseInsensitiveMap<>(request.getUpstreamHeaders());
        logger.info("Receive upstream response, apisix request id {}, headers:{} ", request.getRequestId(), headers);
        // remove the transfer-encoding to make sure no two same value is added.
        // If the upstream add this header, and apisix will add this too. this will cause the outer nginx error:
        //      -> upstream sent duplicate header line: "transfer-encoding: chunked", previous value: "Transfer-Encoding: chunked" while
        //     reading response header from upstream
        response.setHeader("Transfer-Encoding", null);
        String requestId = RequestUtils.getFirstHeaderValue(headers, Constants.HEADER_INTERNAL_REQUEST_ID);
        if (requestId == null) {
            logger.warn("No request id found in request:{}", headers);
        }
        int httpcode = Optional.ofNullable(request.getUpstreamStatusCode()).orElse(500);
        String status = Constants.HEADER_DATA_STATUS_FAIL;
        String logResponseBody = "";
        if (httpcode == 200) {
            status = ObjectUtils.firstNonNull(RequestUtils.getFirstHeaderValue(headers, Constants.HEADER_DATA_STATUS),
                    Constants.HEADER_DATA_STATUS_SUCCESS);
            String responsebody = request.getBody(StandardCharsets.UTF_8);
            // check whether the responsebody is gzipped.
            String zipVersion = RequestUtils.getFirstHeaderValue(headers, Constants.HEADER_ZIP_VERSION);
            if (zipVersion != null && zipVersion.equals(Constants.HEADER_ZIP_VERSION_VALUE_GZIP)) {
                logResponseBody = new String(ZipUtil.unGzip(Base64.getDecoder().decode(responsebody)), StandardCharsets.UTF_8);
            } else {
                logResponseBody = responsebody;
            }

        } else {
            response.setStatusCode(httpcode);
            try {
                // if code is not 200, just return the raw response
                String rawResponseBody = request.getBody(StandardCharsets.UTF_8);
                response.setBody(rawResponseBody);
                response.setHeader("Content-Length", null);
                logResponseBody = request.getBody(StandardCharsets.UTF_8);;
            } catch (Exception e) {
                logger.warn("RecordResponseFilter return non 200 code fail to set response body", e);
            }
        }
        if (requestId != null) {
            response.setHeader(Constants.HEADER_REQUEST_ID, requestId);
            logService.logRequestEnd(requestId, status, httpcode, logResponseBody);
        } else {
            logger.warn("cant find the internal request id in response {} header:{}", request.getRequestId(), headers);
        }

        // Hide some headers, hides the internal information
        for (String clearHeader : Constants.CLEAR_HEADER_WHEN_RESPONSE) {
            response.setHeader(clearHeader, null);
        }

        chain.postFilter(request, response);
    }


    /**
     * If you need to fetch request body in the current plugin, you will need to return true in this function.
     */
    @Override
    public Boolean requiredRespBody() {
        return true;
    }

    /**
     * If you need to fetch request body in the current plugin, you will need to return true in this function.
     */
    @Override
    public Boolean requiredBody() {
        return true;
    }
}
