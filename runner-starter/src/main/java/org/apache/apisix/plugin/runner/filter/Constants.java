package org.apache.apisix.plugin.runner.filter;

public class Constants {
    // note in wolf conf, the header is configured starts with: internal-
    public static final String HEADER_INTERNAL_PREFIX = "internal-";
    public static final String HEADER_USER_ID = HEADER_INTERNAL_PREFIX + "userid";
    public static final String HEADER_DATA_STATUS = HEADER_INTERNAL_PREFIX + "data-status";
    public static final String HEADER_DATA_STATUS_NODATA = "STATUS_NODATA";
    public static final String HEADER_RESPONSEBODY_ENCRYPTED_FLAG = HEADER_INTERNAL_PREFIX + "response-encrypt";
    public static final String HEADER_REQUESTBODY_ENCRYPTED_FLAG = HEADER_INTERNAL_PREFIX + "request-decrypt";

    public static final String ERROR_NOT_FOUND = "Error: user not found";
    public static final String ERROR_DECRYPT_REQUEST_FAILURE = "Error: decrypt request body failure";
}
