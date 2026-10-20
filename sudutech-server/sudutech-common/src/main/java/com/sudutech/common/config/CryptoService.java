package com.sudutech.common.config;

import com.sudutech.common.utils.AesUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 敏感信息加解密服务（数据源密码等），密钥来自 sudutech.security.aes-key
 */
@Component
public class CryptoService {

    private final AesUtils aesUtils;

    public CryptoService(@Value("${sudutech.security.aes-key:}") String key) {
        this.aesUtils = new AesUtils(key);
    }

    public String encrypt(String plain) {
        return aesUtils.encrypt(plain);
    }

    public String decrypt(String cipher) {
        return aesUtils.decrypt(cipher);
    }
}
