package org.apache.apisix.plugin.runner.filter;

import java.util.*;

public class Constants {
    /**
     * note: header names in apisix is lower-case.
     */
    // note in wolf conf, the header is configured starts with: internal-
    public static final String HEADER_INTERNAL_PREFIX = "internal-";
    public static final String HEADER_USER_ID = HEADER_INTERNAL_PREFIX + "userid";
    public static final String HEADER_DATA_STATUS = HEADER_INTERNAL_PREFIX + "data-status";
    public static final String HEADER_SOURCE = HEADER_INTERNAL_PREFIX + "source";
    public static final String HEADER_SOURCE_VALUE_SOURCE_DATA = "SOURCE_DATA";
    public static final String HEADER_DATA_STATUS_NODATA = "STATUS_NODATA";
    public static final String HEADER_DATA_STATUS_SUCCESS = "STATUS_SUCCESS";
    public static final String HEADER_DATA_STATUS_FAIL = "STATUS_FAIL";
    public static final String HEADER_RESPONSEBODY_ENCRYPTED_FLAG = HEADER_INTERNAL_PREFIX + "response-encrypt";
    public static final String HEADER_REQUESTBODY_ENCRYPTED_FLAG = HEADER_INTERNAL_PREFIX + "request-decrypt";
    public static final String HEADER_CONTENT_TYPE = "content-type";
    public static final String HEADER_TYPE_MULTIPART_FORM = "multipart/form-data";
    public static final String HEADER_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String HEADER_FORM_ENCRYPTED_FIELDS = "form-encrypted-fields";
    public static final String HEADER_USERNAME = HEADER_INTERNAL_PREFIX + "username";
    public static final String HEADER_NICKNAME = HEADER_INTERNAL_PREFIX + "nickname";

    public static final String HEADER_REQUEST_ID = "x-request-id";
    public static final String HEADER_INTERNAL_REQUEST_ID = HEADER_INTERNAL_PREFIX + HEADER_REQUEST_ID;

    public static final String ERROR_NOT_FOUND = "Error: user not found";
    public static final String ERROR_DECRYPT_REQUEST_FAILURE = "Error: decrypt request body failure";

    // dont send those headers to user
    public static final List<String> CLEAR_HEADER_WHEN_RESPONSE =  Arrays.asList(
            HEADER_INTERNAL_REQUEST_ID, HEADER_SOURCE, HEADER_REQUESTBODY_ENCRYPTED_FLAG, HEADER_USER_ID, HEADER_USERNAME, HEADER_NICKNAME
    );
}
