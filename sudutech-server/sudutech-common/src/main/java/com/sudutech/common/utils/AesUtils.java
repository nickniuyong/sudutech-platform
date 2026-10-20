package com.sudutech.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 加解密工具（数据源密码等敏感信息加密存储）
 */
public class AesUtils {

    /** 默认开发密钥（生产环境请通过环境变量覆盖 sudutech.security.aes-key） */
    private static final String DEFAULT_KEY = "sudutech@2026##";

    private final SecretKeySpec keySpec;
    private final IvParameterSpec ivSpec;

    public AesUtils(String key) {
        String k = (key == null || key.isEmpty()) ? DEFAULT_KEY : key;
        byte[] keyBytes = new byte[16];
        byte[] raw = k.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(raw, 0, keyBytes, 0, Math.min(raw.length, 16));
        this.keySpec = new SecretKeySpec(keyBytes, "AES");
        this.ivSpec = new IvParameterSpec(keyBytes);
    }

    public String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) {
            return "";
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return "";
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }
}
