package org.apache.apisix.plugin.runner.db.model;

import lombok.*;

import java.io.*;
import java.util.*;

/**
 * user
 * @author 
 */
@Data
public class User implements Serializable {

    /**
     * 对方提供的， 只有公钥，用于加密给对方的数据
     */
    public static final int PROVIDER_OTHER = 1;

    /**
     * 我方生成，有公钥和私钥， 私钥用于解密对方推送给我方的数据
     */
    public static final int PROVIDER_US = 2;

    private Integer id;

    /**
     * wolf中的用户id
     */
    private Integer userid;

    /**
     * 公钥
     */
    private String publickey;

    /**
     * 私钥
     */
    private String privatekey;

    /**
     * 状态：1.正常 0.删除
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private Date gmtcreate;

    /**
     * 修改时间
     */
    private Date gmtmodified;

    private Integer provider;

    public String getPrivatekey() {
        if (provider == PROVIDER_OTHER) {
            throw new IllegalArgumentException("the provider is other, should not call this method " + id);
        }

        return privatekey;
    }

    private static final long serialVersionUID = 1L;
}