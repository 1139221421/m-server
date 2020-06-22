package com.lxl.utils.common;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 加密工具
 * 主要用于传输过程中的数据加密
 * 采用AES/CBC/PKCS5Padding方式
 */
public class SecretUtil {

    private static String DEFAULT_KEY = "LXL_DKEY";
    private static String DEFAULT_IV = "LXL_IV";
    private AES aes;
    private AES customerAes;


    private SecretUtil() {
        aes = new AES(Mode.CBC, Padding.PKCS5Padding, DEFAULT_KEY.getBytes(StandardCharsets.UTF_8), DEFAULT_IV.getBytes(StandardCharsets.UTF_8));
    }

    private static final SecretUtil UTIL = new SecretUtil();

    public static AES getInstance() {
        return UTIL.aes;
    }

    public static AES getInstance(String key) {
        return getInstance(key, null);
    }

    public static AES getInstance(String key, String iv) {
        if (StringUtils.isEmpty(key)) {
            key = DEFAULT_KEY;
        }
        if (StringUtils.isEmpty(iv)) {
            iv = DEFAULT_IV;
        }
        UTIL.customerAes = new AES(Mode.CBC, Padding.PKCS5Padding, key.getBytes(StandardCharsets.UTF_8), iv.getBytes(StandardCharsets.UTF_8));
        return UTIL.customerAes;
    }
}
