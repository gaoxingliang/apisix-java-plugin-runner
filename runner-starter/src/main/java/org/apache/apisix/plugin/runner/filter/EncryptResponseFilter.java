package org.apache.apisix.plugin.runner.filter;

import cn.hutool.core.map.*;
import org.apache.apisix.plugin.runner.*;
import org.apache.apisix.plugin.runner.db.*;
import org.apache.apisix.plugin.runner.db.model.*;
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
        List<String> userIds = headers.get(Constants.HEADER_USER_ID);
        String userId = null;
        if (CollectionUtils.isEmpty(userIds)) {
            logger.warn("No user found in request:{}", headers);
        } else {
            userId = userIds.get(0);
        }

        User user = userService.tryFindUser(userId);
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
            // note it's case SENSITIVE.
            response.setHeader("Content-Length", null);
            response.setStatusCode(200);
            logger.info("EncryptResponseFilter success: user(wolf): userid:{}, encrypted:{}, upstream headers:{}", user.getUserid(), encryptedBody, headers);
        } else {
            logger.warn("EncryptResponseFilter return non 200 code：{}, headers:{}", request.getUpstreamStatusCode(), headers);
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
