package com.lxl.web.utils;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.lxl.common.enums.CodeEnum;
import com.lxl.common.vo.ResponseInfo;

public class ExceptionUtil {
    public static ResponseInfo handleException(BlockException ex) {
        return ResponseInfo.createCodeEnum(CodeEnum.FLOW_ERROR);
    }
}
