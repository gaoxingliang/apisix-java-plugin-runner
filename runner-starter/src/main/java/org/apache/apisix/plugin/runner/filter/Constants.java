package org.apache.apisix.plugin.runner.filter;

public class Constants {
    // note in wolf conf, the header is configured starts with: internal-
    public static final String HEADER_USER_ID = "internal-userid";
    public static final String HEADER_RESPONSEBODY_ENCRYPTED_FLAG = "internal-response-encrypt";
    public static final String HEADER_REQUESTBODY_ENCRYPTED_FLAG = "internal-request-decrypt";

    public static final String ERROR_NOT_FOUND = "Error: user not found";
    public static final String ERROR_DECRYPT_REQUEST_FAILURE = "Error: decrypt request body failure";
}
