package org.apache.apisix.plugin.runner.filter;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.asymmetric.*;
import cn.sichuancredit.apigateway.encryption.*;
import com.alibaba.fastjson.*;
import com.google.common.base.*;
import org.apache.apisix.plugin.runner.db.*;
import org.apache.apisix.plugin.runner.db.model.*;
import org.apache.commons.lang3.*;
import org.bouncycastle.crypto.engines.*;
import org.bouncycastle.jcajce.provider.asymmetric.ec.*;
import org.bouncycastle.jce.provider.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.annotation.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;

@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private Map<String, BigInteger> userSecretMap = new HashMap<>();

    @Autowired
    UserDao userDao;

    /**
     * {@link UserService#extractPrivateKey(String)} 这个需要bc provider
     */
    @PostConstruct
    public void postConstruct() {
        // 添加BC的provider后续要用
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     *
     * @param userIdValue
     * @param provider {@link User#PROVIDER_US} or {@link User#PROVIDER_OTHER}
     * @return
     */
    public User tryFindUser(String userIdValue, int provider) {
        logger.info("输入wolf userid:{}", userIdValue);
        User user = null;
        if (userIdValue == null || userIdValue.trim().isEmpty()) {
            logger.warn("header中未找到用");
        } else {
            try {
                user = userDao.selectByWolfUserId(Integer.valueOf(userIdValue), provider);
            } catch (Exception e) {
                logger.warn("无法获取用户：{}", userIdValue, e);
            }
            if (user == null) {
                logger.warn("未找到用户：{}", userIdValue);
            }
        }

        return user;
    }

    public String decryptBody(String body, User user) throws Exception {
        Preconditions.checkNotNull(user);
        if (StringUtils.isEmpty(body)) {
            return body;
        }
        logger.info("decryptBody:{}, wolfuser:{}", body, user.getUserid());
        EncryptedData data = JSONObject.parseObject(body, EncryptedData.class);
        Preconditions.checkNotNull(data.getData(), "加密数据为NULL");
        Preconditions.checkNotNull(data.getEncryptKey(), "加密数据key为NULL");
        Exception rawException = null;
        String sm4Key = null;
        try {
            sm4Key = MySmUtil.sm2Decrypt(data.getEncryptKey(), user.getPrivatekey());
        } catch (Exception e) {
            rawException = e;
            /**
             * 部分语言框架会只使用原始的privatekey加密。
             * 这里进行单独处理，且是为压缩点格式
             */
            // 尝试用另外方式解密， 这里的bytes是原始的bytes
            // 1 添加非压缩模式， 2 使用原生的privatekey
            BigInteger ecD;
            synchronized (userSecretMap) {
                ecD = userSecretMap.get(user.getPrivatekey());
                if (ecD == null) {
                    ecD = extractPrivateKey(user.getPrivatekey());
                    if (ecD != null) {
                        userSecretMap.put(user.getPrivatekey(), ecD);
                    }
                }
            }
            if (ecD != null) {
                SM2 sm2 = new SM2(ecD.toByteArray(), null);
                sm2.setMode(SM2Engine.Mode.C1C3C2);
                byte [] cipherBytes = Base64.decode(data.getEncryptKey());
                // 创建新的密文数组，添加前缀字节
                byte[] newCipherBytes = new byte[cipherBytes.length + 1];
                newCipherBytes[0] = 0x04; // 添加未压缩点格式的前缀
                System.arraycopy(cipherBytes, 0, newCipherBytes, 1, cipherBytes.length);
                try {
                    sm4Key = new String(sm2.decrypt(newCipherBytes, KeyType.PrivateKey));
                    logger.info("use compatible method to decrypt the key for user:{}", user.getUserid());
                } catch (Exception ex) {
                    logger.error("try other decrypt methods also failed");
                    throw rawException;
                }
            } else {
                throw rawException;
            }
        }

        return MySmUtil.sm4Decrypt(data.getData(), sm4Key);
    }


    /**
     * 从 Base64 编码的 SM2 私钥中提取 256 位整数私钥
     *
     * @param base64PrivateKey Base64 编码的 SM2 私钥
     * @return 256 位整数私钥 NULL if error
     */
    public static BigInteger extractPrivateKey(String base64PrivateKey) {
        try {
            // 解码 Base64 私钥
            byte[] privateKeyBytes = Base64.decode(base64PrivateKey);

            // 使用 BouncyCastle 的 KeyFactory 解析私钥
            KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            BCECPrivateKey privateKey = (BCECPrivateKey) keyFactory.generatePrivate(keySpec);

            return privateKey.getD();
        } catch (Exception e) {
            return null;
        }
    }

    public String encryptBody(String body, User user, String dataStatus) {
        Preconditions.checkNotNull(user);
        if (StringUtils.isEmpty(body)) {
            return body;
        }
        // 随机生成Sm4密钥
        String sm4Key = MySmUtil.generateSm4Key();
        // 国密Sm2公钥加密Sm4秘钥
        String encryptKey = MySmUtil.sm2Encrypt(sm4Key, user.getPublickey());
        // Sm4加密传输数据
        String data = MySmUtil.sm4Encrypt(body, sm4Key);
        EncryptedData encryptedData = new EncryptedData();
        encryptedData.setData(data);
        encryptedData.setEncryptKey(encryptKey);
        encryptedData.setDataStatus(ObjectUtils.firstNonNull(dataStatus, Constants.HEADER_DATA_STATUS_SUCCESS));

        logger.info("encryptBody:{}, wolfuser:{}", StringUtils.abbreviate(data, 128), user.getUserid());
        return JSONObject.toJSONString(encryptedData);
    }
}
