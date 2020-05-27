package com.lxl.common.vo;

import com.alibaba.fastjson.JSONObject;
import com.lxl.common.enums.CodeEnum;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ResponseInfo<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String message;

    protected Boolean success;

    protected String code;

    protected Map<String, Object> data = new HashMap<String, Object>();

    protected T businessData;

    public T getBusinessData() {
        return businessData;
    }

    public ResponseInfo<T> setBusinessData(T businessData) {
        this.businessData = businessData;
        return this;
    }

    public ResponseInfo<T> setCodeEnum(CodeEnum codeEnum) {
        this.code = codeEnum.code;
        this.message = codeEnum.message;
        return this;
    }

    public static ResponseInfo createSuccess() {
        return createCodeEnum(CodeEnum.SUCCESS);
    }

    public static <T> ResponseInfo<T> createSuccess(T data) {
        ResponseInfo<T> success = createSuccess();
        success.setBusinessData(data);
        return success;
    }

    public static ResponseInfo createCodeEnum(CodeEnum codeEnum) {
        ResponseInfo responseInfo = new ResponseInfo<>(true);
        if (Integer.parseInt(codeEnum.getCode()) < 0) {
            responseInfo.setSuccess(false);
        }
        responseInfo.setCode(codeEnum.getCode());
        responseInfo.setMessage(codeEnum.getMessage());
        return responseInfo;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ResponseInfo() {
    }

    public ResponseInfo(Boolean success) {
        this.success = success;
    }

    public ResponseInfo(String code, String message) {
        this.code = code;
        this.message = message;
        success = false;
    }

    public ResponseInfo addData(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public ResponseInfo setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public ResponseInfo<T> setSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public ResponseInfo<T> setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }
}
