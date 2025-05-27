package org.apache.apisix.plugin.runner.db.model;

import lombok.*;

import java.io.*;
import java.util.*;

@Data
public class KafkaDelayAccessRecord implements Serializable {
    private Integer id;

    /**
     * 目标topic
     */
    private String topic;

    /**
     * 内容
     */
    private String content;

    /**
     * 服务器创建时间
     */
    private Date gmtCreate = new Date();

    private static final long serialVersionUID = 1L;
}