package com.lxl.utils.common;

/**
 * id处理
 *
 * @author
 */
public class IdUtil {
    /**
     * 默认id转换值
     */
    public static Long WRAP_ID = 4388067324078788L;

    public static Long wrap(Long id) {
        if (id == null || id > WRAP_ID) {
            return id;
        }
        return WRAP_ID + id;
    }

    public static Long unwrap(Long wrapId) {
        if (wrapId == null || wrapId < WRAP_ID) {
            return wrapId;
        }
        return wrapId - WRAP_ID;
    }
}
