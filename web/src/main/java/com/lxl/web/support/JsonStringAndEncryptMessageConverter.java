package com.lxl.web.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.lxl.utils.common.SecretUtil;
import com.lxl.utils.config.ConfUtil;
import com.lxl.web.utils.HttpServletUtils;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 输出加密处理
 */
public class JsonStringAndEncryptMessageConverter extends FastJsonHttpMessageConverter {

    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        //默认加密数据
        if (!Boolean.parseBoolean(ConfUtil.getPropertyOrDefault("enable_encrypt", "false"))) {
            super.writeInternal(object, outputMessage);
            return;
        }
        try {
            RequestContextHolder.currentRequestAttributes();
        } catch (IllegalStateException e) {
            //发生异常，获取request不成功
            super.writeInternal(object, outputMessage);
            return;
        }
        if (HttpServletUtils.getRequest().getHeader("feign-request") != null) {
            // 如果携带feign-request标识，不需要加密
            super.writeInternal(object, outputMessage);
            return;
        }
        try (ByteArrayOutputStream outnew = new ByteArrayOutputStream()) {
            String encryptHex = SecretUtil.getInstance().encryptHex(JSON.toJSONString(object, this.getFastJsonConfig().getSerializeFilters(), this.getFastJsonConfig().getSerializerFeatures()));
            outnew.write(encryptHex.getBytes(StandardCharsets.UTF_8));
            outnew.writeTo(outputMessage.getBody());
        }
    }
}
