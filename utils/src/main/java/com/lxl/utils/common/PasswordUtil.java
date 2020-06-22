package com.lxl.utils.common;

import org.apache.commons.codec.binary.Hex;

/**
 * 密码工具类
 */
public class PasswordUtil {

    /**
     * 取得用户密码
     *
     * @param prefix
     * @param pwd
     * @param suffix
     * @return
     */
    public static String getPwd(String prefix, String pwd, String suffix) {
        try {
            String md5PWD = Hex.encodeHexString(CodeUtil.encryptMD5(pwd.getBytes()));
            String str = prefix + md5PWD + suffix;
            return Hex.encodeHexString(CodeUtil.encryptSHA(str.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPwd(String prefix, String pwd) {
        return getPwd(prefix, pwd, "");
    }


}
