package com.lxl.web.utils;

import com.alibaba.csp.sentinel.slots.block.BlockException;

public class ExceptionUtil {
    public static String handleException(BlockException ex) {
        return "服务器繁忙，请稍后重试。。。";
    }
}
