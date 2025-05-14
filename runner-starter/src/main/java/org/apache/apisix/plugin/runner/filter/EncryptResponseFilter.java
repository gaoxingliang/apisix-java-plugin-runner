package org.apache.apisix.plugin.runner.filter;

import cn.hutool.core.map.*;
import org.apache.apisix.plugin.runner.*;
import org.apache.apisix.plugin.runner.db.*;
import org.apache.apisix.plugin.runner.db.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

import java.nio.charset.*;
import java.util.*;

@Component
public class EncryptResponseFilter implements PluginFilter {
    private final Logger logger = LoggerFactory.getLogger(EncryptResponseFilter.class);

    @Autowired
    UserService userService;

    @Autowired
    UserDao userDao;

    @Override
    public String name() {
        return "EncryptResponseFilter";
    }

    @Override
    public void postFilter(PostRequest request, PostResponse response, PluginFilterChain chain) {
        Map<String, List<String>> headers = new CaseInsensitiveMap<>(request.getUpstreamHeaders());
        logger.info("Receive upstream response, headers:{}", headers);
        List<String> userIds = headers.get(Constants.HEADER_USER_ID);
        String userId = null;
        if (CollectionUtils.isEmpty(userIds)) {
            logger.warn("No user found in request:{}", headers);
        } else {
            userId = userIds.get(0);
        }

        User user = userService.tryFindUser(userId, User.PROVIDER_OTHER);
        // remove the transfer-encoding to make sure no two same value is added.
        // If the upstream add this header, and apisix will add this too. this will cause the outer nginx error:
        //      -> upstream sent duplicate header line: "transfer-encoding: chunked", previous value: "Transfer-Encoding: chunked" while reading response header from upstream
        response.setHeader("Transfer-Encoding", null);
        if (user == null) {
            response.setStatusCode(403);
            response.setBody(Constants.ERROR_NOT_FOUND);
            logger.warn("not found the user, maybe disabled. wolfuserid: {}", userId);
        } else if (request.getUpstreamStatusCode() == 200) {
            List<String> status = headers.get(Constants.HEADER_DATA_STATUS);
            String dataStatus = null;
            if (status != null && status.size() >= 1) {
                dataStatus = status.get(0);
            }
            String encryptedBody = userService.encryptBody(request.getBody(Charset.forName("UTF-8")), user, dataStatus);
            response.setBody(encryptedBody);
            // remove the header because the length is mismatch after encrypted.
            // note it's case SENSITIVE. remove this header
            response.setHeader("Content-Length", null);
            response.setStatusCode(200);
            logger.info("EncryptResponseFilter success: user(wolf): userid:{}, encrypted:{}, upstream headers:{}",
                    user.getUserid(),
                    StringUtils.abbreviate(encryptedBody, 512), headers
            );
        } else {
            logger.warn("EncryptResponseFilter return non 200 code：{}, headers:{}", request.getUpstreamStatusCode(), headers);
            response.setStatusCode(Optional.ofNullable(request.getUpstreamStatusCode()).orElse(500));
            try {
                // if code is not 200, just return the raw response
                String rawResponseBody = request.getBody(Charset.forName("UTF-8"));
                logger.warn("EncryptResponseFilter return non 200 code：{}, headers:{}, raw response body:{}",  request.getUpstreamStatusCode(), headers, rawResponseBody);
                response.setBody(rawResponseBody);
                response.setHeader("Content-Length", null);
            } catch (Exception e){
                logger.warn("EncryptResponseFilter return non 200 code fail to set response body", e);
            }
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
