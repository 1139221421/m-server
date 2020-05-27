package com.lxl.web.handler;

import com.lxl.common.enums.CodeEnum;
import com.lxl.common.vo.ResponseInfo;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import feign.RetryableException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({Exception.class})
    @ResponseBody
    public ResponseInfo handleException(Exception e) {
        ResponseInfo result = new ResponseInfo(CodeEnum.SERVICE_ERROR.getCode(), "");
        String msg = CodeEnum.SERVICE_ERROR.getMessage();
        if (e != null) {
            logger.error("", e);
            result.addData("error", e.getMessage());
            if (e instanceof HystrixRuntimeException || e instanceof RetryableException) {
                msg = CodeEnum.TIME_OUT.getMessage();
            } else if (StringUtils.isNotEmpty(e.getMessage()) && e.getMessage().length() < 50) {
                msg = e.getMessage();
            }
        }
        result.setMessage(msg);
        return result;
    }
}
