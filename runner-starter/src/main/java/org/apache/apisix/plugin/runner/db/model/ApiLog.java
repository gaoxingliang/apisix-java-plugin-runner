package org.apache.apisix.plugin.runner.db.model;

import lombok.*;
import org.apache.commons.lang3.*;

@Data
public class ApiLog {
    private static final int REQUEST_PARAMETERS_MAX_LENGTH = 256;
    private static final int REQUEST_BODY_MAX_LENGTH = 256;
    private static final int RESPONSE_MAX_LENGTH = 1024 * 1024;

    private final long timestamp = System.currentTimeMillis();
    private String rawkey;
    private String path;
    private String method;
    private int userid;
    private String requestParameters = "";
    private String requestBody = "";
    private String status;
    // in mills
    private long elapse;
    private String response = "";
    private int responseLength = 0;
    private String source = "SOURCE_DATA";
    // http code
    private int code;
    private String requestId;
    private String ip = "";

    @Override
    public String toString() {
        return "ApiLog{" +
                "timestamp=" + timestamp +
                ", rawkey='" + rawkey + '\'' +
                ", path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", userid=" + userid +
                ", requestParameters='" + StringUtils.abbreviate(requestParameters, 64) + '\'' +
                ", requestBody='" + StringUtils.abbreviate(requestBody, 64) + '\'' +
                ", status='" + status + '\'' +
                ", elapse=" + elapse +
                ", response='" + StringUtils.abbreviate(response, 64) + '\'' +
                ", code=" + code +
                ", requestId='" + requestId + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }

    public void setRequestParameters(String requestParameters) {
        this.requestParameters = StringUtils.abbreviate(requestParameters, REQUEST_PARAMETERS_MAX_LENGTH);
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = StringUtils.abbreviate(requestBody, REQUEST_BODY_MAX_LENGTH);
    }

    public void setResponse(String response) {
        this.response = StringUtils.abbreviate(response, RESPONSE_MAX_LENGTH);
        this.responseLength = response == null ? 0 : response.length();
    }
}
